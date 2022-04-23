package com.ddf.materialbintool;

import com.beust.jcommander.JCommander;
import com.ddf.materialbintool.bgfx.BgfxShader;
import com.ddf.materialbintool.materials.CompiledMaterialDefinition;
import com.ddf.materialbintool.materials.PlatformShaderStage;
import com.ddf.materialbintool.materials.definition.FlagMode;
import com.ddf.materialbintool.util.ByteBufUtil;
import com.ddf.materialbintool.util.FileUtil;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static boolean addFlagModesToCode = false;

    public static void main(String[] args) {
    	Args args1 = new Args();
		JCommander.newBuilder()
				.addObject(args1)
				.build()
				.parse(args);

		addFlagModesToCode = args1.addFlagModesToCode;

		File inputFile = new File(args1.inputPath);
		if (!inputFile.exists())
			return;

		String fileName = inputFile.getName();
		if (inputFile.isDirectory()) {
			File jsonFile = new File(inputFile, fileName + ".json");
			if (!jsonFile.exists())
				return;

			CompiledMaterialDefinition cmd = loadCompiledMaterialDefinition(jsonFile);
			ByteBuf buf = ByteBufUtil.buffer();
			cmd.saveTo(buf);
			FileUtil.write(new File(inputFile, fileName + ".material.bin"), ByteBufUtil.toByteArray(buf));
		} else {
			if (fileName.endsWith(".json")) {
				CompiledMaterialDefinition cmd = loadCompiledMaterialDefinition(inputFile);
				ByteBuf buf = ByteBufUtil.buffer();
				cmd.saveTo(buf);

				String name = fileName.substring(0, fileName.indexOf(".json")) + ".material.bin";
				FileUtil.write(new File(inputFile.getParentFile(), name), ByteBufUtil.toByteArray(buf));
			} else if (fileName.endsWith(".material.bin")) {
				ByteBuf buf = ByteBufUtil.wrappedBuffer(FileUtil.readAllBytes(inputFile));
				CompiledMaterialDefinition cmd = new CompiledMaterialDefinition();
				cmd.loadFrom(buf);

				String name = fileName.substring(0, fileName.indexOf(".material.bin"));
				saveCompiledMaterialDefinition(cmd, name, new File(inputFile.getParentFile(), name));
			}
		}
    }

	public static void saveCompiledMaterialDefinition(CompiledMaterialDefinition cmd, String name, File outputDir) {
    	if (!outputDir.exists() && !outputDir.mkdirs())
    		return;

		JsonObject jsonObject = GSON.toJsonTree(cmd).getAsJsonObject();
		JsonArray passes = new JsonArray();
		for (Map.Entry<String, CompiledMaterialDefinition.Pass> entry : cmd.passMap.entrySet()) {
			String passName = entry.getKey();
			savePass(entry.getValue(), passName, new File(outputDir, passName));
			passes.add(passName);
		}
		jsonObject.add("passes", passes);

		FileUtil.writeString(new File(outputDir, name + ".json"), GSON.toJson(jsonObject));
	}

	private static void savePass(CompiledMaterialDefinition.Pass pass, String passName, File outputDir) {
		if (!outputDir.exists() && !outputDir.mkdirs())
			return;

		JsonObject jsonObject = GSON.toJsonTree(pass).getAsJsonObject();
		JsonArray variantList = jsonObject.get("variantList").getAsJsonArray();
		for (int i = 0; i < pass.variantList.size(); ++i) {
			CompiledMaterialDefinition.Variant variant = pass.variantList.get(i);
			JsonArray shaderCodes = new JsonArray();
			for (Map.Entry<PlatformShaderStage, CompiledMaterialDefinition.ShaderCode> entry : variant.shaderCodeMap.entrySet()) {
				PlatformShaderStage platformShaderStage = entry.getKey();
				CompiledMaterialDefinition.ShaderCode shaderCode = entry.getValue();

				JsonObject entryJson = new JsonObject();
				entryJson.add("platformShaderStage", GSON.toJsonTree(platformShaderStage));

				JsonObject shaderCodeJson = GSON.toJsonTree(shaderCode).getAsJsonObject();
				BgfxShader bgfxShader = BgfxShader.create(platformShaderStage.platform);
				bgfxShader.read(shaderCode.bgfxShaderData);

				String fileName = i + "." + toFileName(platformShaderStage);
				byte[] code = bgfxShader.getCode();
				if (addFlagModesToCode && (platformShaderStage.platform.startsWith("GLSL") || platformShaderStage.platform.startsWith("ESSL"))) {
					StringBuilder sb = new StringBuilder();
					List<FlagMode> flagModeList = new ArrayList<>(variant.flagModeList);
					flagModeList.sort(Comparator.comparing(FlagMode::getKey));
					for (FlagMode flagMode : flagModeList) {
						sb.append("//");
						sb.append(flagMode.getKey()).append("=").append(flagMode.getValue());
						sb.append("\n");
					}
					sb.append("\n");
					sb.append(new String(code, StandardCharsets.UTF_8));
					code = sb.toString().getBytes(StandardCharsets.UTF_8);
				}
				FileUtil.write(new File(outputDir, fileName), code);

				JsonObject bgfxShaderJson = GSON.toJsonTree(bgfxShader).getAsJsonObject();
				bgfxShaderJson.addProperty("codeFile", fileName);

				shaderCodeJson.add("bgfxShaderData", bgfxShaderJson);

				entryJson.add("shaderCode", shaderCodeJson);

				shaderCodes.add(entryJson);
			}
			variantList.get(i).getAsJsonObject().add("shaderCodes", shaderCodes);
		}

		FileUtil.writeString(new File(outputDir, passName + ".json"), GSON.toJson(jsonObject));
	}

	public static CompiledMaterialDefinition loadCompiledMaterialDefinition(File jsonFile) {
    	File inputDir = jsonFile.getParentFile();
    	JsonElement jsonElement = JsonParser.parseString(FileUtil.readString(jsonFile));
    	CompiledMaterialDefinition cmd = GSON.fromJson(jsonElement, CompiledMaterialDefinition.class);
    	cmd.passMap = new LinkedHashMap<>();

    	JsonArray passes = jsonElement.getAsJsonObject().get("passes").getAsJsonArray();
    	for (int i = 0; i < passes.size(); ++i) {
    		String passName = passes.get(i).getAsString();
    		cmd.passMap.put(passName, loadPass(new File(inputDir, passName + File.separator + passName + ".json")));
		}
		return cmd;
	}

	private static CompiledMaterialDefinition.Pass loadPass(File jsonFile) {
		File inputDir = jsonFile.getParentFile();
		JsonElement jsonElement = JsonParser.parseString(FileUtil.readString(jsonFile));
		CompiledMaterialDefinition.Pass pass = GSON.fromJson(jsonElement, CompiledMaterialDefinition.Pass.class);

		JsonArray variantList = jsonElement.getAsJsonObject().get("variantList").getAsJsonArray();
		for (int i = 0; i < pass.variantList.size(); ++i) {
			CompiledMaterialDefinition.Variant variant = pass.variantList.get(i);
			variant.shaderCodeMap = new LinkedHashMap<>();

			JsonArray shaderCodes = variantList.get(i).getAsJsonObject().get("shaderCodes").getAsJsonArray();
			for (int j = 0; j < shaderCodes.size(); ++j) {
				JsonObject entryJson = shaderCodes.get(j).getAsJsonObject();

				PlatformShaderStage platformShaderStage = GSON.fromJson(entryJson.get("platformShaderStage"), PlatformShaderStage.class);
				CompiledMaterialDefinition.ShaderCode shaderCode = GSON.fromJson(entryJson.get("shaderCode"), CompiledMaterialDefinition.ShaderCode.class);

				JsonObject bgfxShaderData = entryJson.get("shaderCode").getAsJsonObject().get("bgfxShaderData").getAsJsonObject();
				BgfxShader bgfxShader = GSON.fromJson(bgfxShaderData, BgfxShader.getClass(platformShaderStage.platform));
				bgfxShader.setCode(FileUtil.readAllBytes(new File(inputDir, bgfxShaderData.get("codeFile").getAsString())));

				shaderCode.bgfxShaderData = bgfxShader.toByteArray();

				variant.shaderCodeMap.put(platformShaderStage, shaderCode);
			}
		}

		return pass;
	}

	private static String toFileName(PlatformShaderStage platformShaderStage) {
		String fileName = platformShaderStage.platform + "." + platformShaderStage.type;
		if (platformShaderStage.platform.startsWith("Direct3D"))
			fileName += ".dxbc";
		else if (platformShaderStage.platform.startsWith("GLSL") || platformShaderStage.platform.startsWith("ESSL"))
			fileName += ".glsl";
		return fileName;
	}
}
