package com.ddf.materialbintool.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.ddf.materialbintool.bgfx.BgfxShader;
import com.ddf.materialbintool.main.compiler.BgfxShaderCompiler;
import com.ddf.materialbintool.main.compiler.Defines;
import com.ddf.materialbintool.main.util.StringUtil;
import com.ddf.materialbintool.main.util.UsageFormatter;
import com.ddf.materialbintool.materials.CompiledMaterialDefinition;
import com.ddf.materialbintool.materials.PlatformShaderStage;
import com.ddf.materialbintool.materials.definition.EncryptionVariants;
import com.ddf.materialbintool.materials.definition.FlagMode;
import com.ddf.materialbintool.main.util.FileUtil;
import com.ddf.materialbintool.util.ByteBuf;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static void main(String[] args1) {
		Args args = new Args();
		JCommander jCommander = JCommander.newBuilder()
				.programName(getProgramName())
				.addObject(args)
				.build();
		jCommander.setUsageFormatter(new UsageFormatter(jCommander));

		try {
			jCommander.parse(args1);
		} catch (ParameterException e) {
			jCommander.usage();
			System.out.println("Error: " + e.getMessage());
			return;
		}

		if (args.help) {
			jCommander.usage();
			return;
		}

		File inputFile = new File(args.inputPath);
		if (!inputFile.exists()) {
			System.out.println("Error: input file does not exist");
			return;
		}

		if (args.unpack) {
			if (inputFile.isFile() && inputFile.getName().endsWith(".material.bin")) {
				System.out.println("Unpacking " + inputFile.getName());
				unpack(inputFile, args.outputPath, args.addFlagModesToCode, args.raw);
			} else if (inputFile.isDirectory()) {
				for (File file : inputFile.listFiles()) {
					if (file.getName().endsWith(".material.bin")) {
						System.out.println("Unpacking " + file.getName());
						unpack(file, args.outputPath, args.addFlagModesToCode, args.raw);
					}
				}
			} else {
				System.out.println("Error: the input file is not a .material.bin file or directory");
			}
		} else if (args.repack) {
			File jsonFile = getInputJsonFile(inputFile);
			File outputFile = getRepackOutputFile(inputFile, args.outputPath);
			if (jsonFile == null) {
				System.out.println("Error: failed to get input file");
				return;
			}
			if (outputFile == null) {
				System.out.println("Error: failed to create output file");
				return;
			}

			System.out.println("Repacking " + outputFile.getName());
			CompiledMaterialDefinition cmd = loadCompiledMaterialDefinition(jsonFile, true, args.raw);
			ByteBuf buf = new ByteBuf();
			cmd.saveTo(buf, args.encrypt ? EncryptionVariants.SimplePassphrase : EncryptionVariants.None);
			FileUtil.write(outputFile, buf.toByteArray());
		} else if (args.compile) {
			File jsonFile = getInputJsonFile(inputFile);
			File outputFile = getRepackOutputFile(inputFile, args.outputPath);
			if (jsonFile == null) {
				System.out.println("Error: failed to get input file");
				return;
			}
			if (outputFile == null) {
				System.out.println("Error: failed to create output file");
				return;
			}

			System.out.println("Compiling " + outputFile.getName());

			String name = jsonFile.getName().substring(0, jsonFile.getName().indexOf(".json"));
			File parent = jsonFile.getParentFile();
			File srcDir = new File(parent, "src");
			File varyingDefFile = new File(srcDir, name + ".varying.def.sc");
			File vertexSourceFile = new File(srcDir, name + ".vertex.sc");
			File fragmentSourceFile = new File(srcDir, name + ".fragment.sc");
			File computeSourceFile = new File(srcDir, name + ".compute.sc");

			File definesJsonFile = new File(parent, "defines.json");
			JsonObject passesDefines = null;
			JsonObject flagModesDefines = null;
			if (definesJsonFile.exists()) {
				JsonObject definesJson = JsonParser.parseString(FileUtil.readString(definesJsonFile)).getAsJsonObject();
				if (definesJson.has("passes"))
					passesDefines = definesJson.getAsJsonObject("passes");
				if (definesJson.has("flagModes"))
					flagModesDefines = definesJson.getAsJsonObject("flagModes");
			}

			String compilerPath = findCompilerPath(args.shaderCompilerPath);
			if (compilerPath == null) {
				System.out.println("Error: shaderc not found");
				return;
			}
			BgfxShaderCompiler compiler = new BgfxShaderCompiler(compilerPath);
			if (args.includePath != null) {
				for (String path : args.includePath) {
					compiler.addIncludePath(path);
				}
			}
			CompiledMaterialDefinition cmd = loadCompiledMaterialDefinition(jsonFile, true, args.raw);
			for (Map.Entry<String, CompiledMaterialDefinition.Pass> passEntry : cmd.passMap.entrySet()) {
				String passName = passEntry.getKey();
				System.out.println("Compiling " + passName);
				for (CompiledMaterialDefinition.Variant variant : passEntry.getValue().variantList) {
					Iterator<Map.Entry<PlatformShaderStage, CompiledMaterialDefinition.ShaderCode>> iterator = variant.shaderCodeMap.entrySet().iterator();
					while (iterator.hasNext()) {
						Map.Entry<PlatformShaderStage, CompiledMaterialDefinition.ShaderCode> entry = iterator.next();

						PlatformShaderStage platformShaderStage = entry.getKey();
						CompiledMaterialDefinition.ShaderCode shaderCode = entry.getValue();

						Defines defines = new Defines();
						defines.addDefine("BGFX_CONFIG_MAX_BONES", "4");

						if (passesDefines != null && passesDefines.has(passName)) {
							for (JsonElement element : passesDefines.getAsJsonArray(passName)) {
								defines.addDefine(element.getAsString());
							}
						} else {
							defines.addDefine(StringUtil.toUnderScore(passName));
						}

						for (FlagMode flagMode : variant.flagModeList) {
							if (flagModesDefines != null && flagModesDefines.has(flagMode.getKey())) {
								JsonObject flag = flagModesDefines.getAsJsonObject(flagMode.getKey());
								if (flag.has(flagMode.getValue())) {
									for (JsonElement element : flag.getAsJsonArray(flagMode.getValue())) {
										defines.addDefine(element.getAsString());
									}
								}
							} else if ("On".equals(flagMode.getValue())) {
								defines.addDefine(StringUtil.toUnderScore(flagMode.getKey()));
							}
						}

						File input;
						switch (platformShaderStage.stage) {
							case Vertex:
								input = vertexSourceFile;
								break;
							case Fragment:
								input = fragmentSourceFile;
								break;
							case Compute:
								input = computeSourceFile;
								break;
							case Unknown:
							default:
								input = fragmentSourceFile;
								break;
						}
						byte[] compiled = compiler.compile(input, varyingDefFile, defines, platformShaderStage.platform, platformShaderStage.stage);
						if (compiled != null) {
							shaderCode.bgfxShaderData = compiled;
						}
						if (shaderCode.bgfxShaderData == null) {
							iterator.remove();
						}
					}
				}
			}

			ByteBuf buf = new ByteBuf();
			cmd.saveTo(buf, args.encrypt ? EncryptionVariants.SimplePassphrase : EncryptionVariants.None);
			FileUtil.write(outputFile, buf.toByteArray());
		} else {
			jCommander.usage();
		}
    }

	public static String findCompilerPath(String compilerPath) {
		if (compilerPath != null) {
			File file = new File(compilerPath);
			if (file.exists() && file.canExecute()) {
				return compilerPath;
			}
		}
		try {
			Runtime.getRuntime().exec("shaderc");
			return "shaderc";
		} catch (IOException ignored) {}

		try {
			Runtime.getRuntime().exec("shaderc.exe");
			return "shaderc.exe";
		} catch (IOException ignored) {}

		return null;
	}

    private static String getProgramName() {
		String jarName = "MaterialBinTool.jar";
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (path.endsWith(".jar")) {
			jarName = path.substring(path.lastIndexOf("/") + 1);
		}
		return "java -jar " + jarName;
	}

    private static File getInputJsonFile(File inputFile) {
		String fileName = inputFile.getName();
		if (inputFile.isDirectory()) {
			File jsonFile = new File(inputFile, fileName + ".json");
			if (!jsonFile.exists()) {
				return null;
			}
			return jsonFile;
		} else if (fileName.endsWith(".json")){
			return inputFile;
		}
		return null;
	}

	private static File getRepackOutputFile(File inputFile, String outputDirPath) {
		String fileName = inputFile.getName();
		String name;
		if (inputFile.isDirectory()) {
			name = fileName + ".material.bin";
		} else if (fileName.endsWith(".json")) {
			name = fileName.substring(0, fileName.indexOf(".json")) + ".material.bin";
		} else {
			return null;
		}

		if (outputDirPath != null) {
			File dir = new File(outputDirPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			return new File(dir, name);
		}

		if (inputFile.isDirectory()) {
			return new File(inputFile, name);
		} else {
			return new File(inputFile.getParentFile(), name);
		}
	}

	public static void unpack(File inputFile, String outputDirPath, boolean addFlagModesToCode, boolean raw) {
		ByteBuf buf = new ByteBuf(FileUtil.readAllBytes(inputFile));
		CompiledMaterialDefinition cmd = new CompiledMaterialDefinition();
		cmd.loadFrom(buf);

		String fileName = inputFile.getName();
		String name = fileName.substring(0, fileName.indexOf(".material.bin"));
		File outputFile = outputDirPath != null ? new File(outputDirPath, name) : new File(inputFile.getParentFile(), name);
		saveCompiledMaterialDefinition(cmd, name, outputFile, addFlagModesToCode, raw);
	}

	public static void saveCompiledMaterialDefinition(CompiledMaterialDefinition cmd, String name, File outputDir, boolean addFlagModesToCode, boolean raw) {
    	if (!outputDir.exists() && !outputDir.mkdirs())
    		return;

		JsonObject jsonObject = GSON.toJsonTree(cmd).getAsJsonObject();
		JsonArray passes = new JsonArray();
		for (Map.Entry<String, CompiledMaterialDefinition.Pass> entry : cmd.passMap.entrySet()) {
			String passName = entry.getKey();
			savePass(entry.getValue(), passName, new File(outputDir, passName), addFlagModesToCode, raw);
			passes.add(passName);
		}
		jsonObject.add("passes", passes);

		FileUtil.writeString(new File(outputDir, name + ".json"), GSON.toJson(jsonObject));
	}

	private static void savePass(CompiledMaterialDefinition.Pass pass, String passName, File outputDir, boolean addFlagModesToCode, boolean raw) {
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
				if (!raw) {
					BgfxShader bgfxShader = BgfxShader.create(platformShaderStage.platformName);
					bgfxShader.read(shaderCode.bgfxShaderData);

					String fileName = i + "." + toFileName(platformShaderStage, false);
					byte[] code = bgfxShader.getCode();
					if (addFlagModesToCode && (platformShaderStage.platformName.startsWith("GLSL") || platformShaderStage.platformName.startsWith("ESSL") || platformShaderStage.platformName.startsWith("Metal"))) {
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
				} else {
					String fileName = i + "." + toFileName(platformShaderStage, true);
					FileUtil.write(new File(outputDir, fileName), shaderCode.bgfxShaderData);
					shaderCodeJson.addProperty("bgfxShaderData", fileName);
				}

				entryJson.add("shaderCode", shaderCodeJson);

				shaderCodes.add(entryJson);
			}
			variantList.get(i).getAsJsonObject().add("shaderCodes", shaderCodes);
		}

		FileUtil.writeString(new File(outputDir, passName + ".json"), GSON.toJson(jsonObject));
	}

	public static CompiledMaterialDefinition loadCompiledMaterialDefinition(File jsonFile, boolean loadCode, boolean raw) {
    	File inputDir = jsonFile.getParentFile();
    	JsonElement jsonElement = JsonParser.parseString(FileUtil.readString(jsonFile));
    	CompiledMaterialDefinition cmd = GSON.fromJson(jsonElement, CompiledMaterialDefinition.class);
    	cmd.passMap = new LinkedHashMap<>();

    	JsonArray passes = jsonElement.getAsJsonObject().get("passes").getAsJsonArray();
    	for (int i = 0; i < passes.size(); ++i) {
    		String passName = passes.get(i).getAsString();
    		cmd.passMap.put(passName, loadPass(new File(inputDir, passName + File.separator + passName + ".json"), loadCode, raw));
		}
		return cmd;
	}

	private static CompiledMaterialDefinition.Pass loadPass(File jsonFile, boolean loadCode, boolean raw) {
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

				if (loadCode) {
					if (!raw) {
						JsonObject bgfxShaderData = entryJson.getAsJsonObject("shaderCode").getAsJsonObject("bgfxShaderData");
						BgfxShader bgfxShader = GSON.fromJson(bgfxShaderData, BgfxShader.getClass(platformShaderStage.platformName));
						bgfxShader.setCode(FileUtil.readAllBytes(new File(inputDir, bgfxShaderData.get("codeFile").getAsString())));
						shaderCode.bgfxShaderData = bgfxShader.toByteArray();
					} else {
						String path = entryJson.getAsJsonObject("shaderCode").get("bgfxShaderData").getAsString();
						shaderCode.bgfxShaderData = FileUtil.readAllBytes(new File(inputDir, path));
					}
				}

				variant.shaderCodeMap.put(platformShaderStage, shaderCode);
			}
		}

		return pass;
	}

	private static String toFileName(PlatformShaderStage platformShaderStage, boolean raw) {
		String fileName = platformShaderStage.platformName + "." + platformShaderStage.stageName;
		if (raw)
			fileName += ".bin";
		else if (platformShaderStage.platformName.startsWith("Direct3D"))
			fileName += ".dxbc";
		else if (platformShaderStage.platformName.startsWith("GLSL") || platformShaderStage.platformName.startsWith("ESSL"))
			fileName += ".glsl";
		else if (platformShaderStage.platformName.startsWith("Metal"))
			fileName += ".metal";
		else if (platformShaderStage.platformName.startsWith("Vulkan"))
			fileName += ".spirv";
		return fileName;
	}
}
