package com.ddf.materialbintool.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.ddf.materialbintool.bgfx.BgfxShader;
import com.ddf.materialbintool.main.compiler.BgfxShaderCompiler;
import com.ddf.materialbintool.main.compiler.Defines;
import com.ddf.materialbintool.main.compiler.VaryingDefPreprocessor;
import com.ddf.materialbintool.main.util.StringUtil;
import com.ddf.materialbintool.main.util.UsageFormatter;
import com.ddf.materialbintool.materials.CompiledMaterialDefinition;
import com.ddf.materialbintool.materials.PlatformShaderStage;
import com.ddf.materialbintool.materials.definition.EncryptionVariants;
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

		if (args.unpack) {
			unpack(args);
		} else if (args.repack) {
			repack(args);
		} else if (args.compile) {
			compile(args);
		} else if (args.mergeData) {
			mergeData(args);
		} else {
			jCommander.usage();
		}
    }

	private static void unpack(Args args) {
		for (String path : args.inputPath) {
			File inputFile = new File(path);
			if (!inputFile.exists()) {
				System.out.println("Error: input file does not exist");
				continue;
			}

			if (inputFile.isFile() && inputFile.getName().endsWith(".material.bin")) {
				System.out.println("Unpacking " + inputFile.getName());
				unpack(inputFile, args.outputPath, args.addFlagsToCode, args.raw, args.dataOnly);
			} else if (inputFile.isDirectory()) {
				for (File file : inputFile.listFiles()) {
					if (file.getName().endsWith(".material.bin")) {
						System.out.println("Unpacking " + file.getName());
						unpack(file, args.outputPath, args.addFlagsToCode, args.raw, args.dataOnly);
					}
				}
			} else {
				System.out.println("Error: the input file is not a .material.bin file or directory");
			}
		}
	}

	private static void repack(Args args) {
		for (String path : args.inputPath) {
			File inputFile = new File(path);
			if (!inputFile.exists()) {
				System.out.println("Error: input file does not exist");
				continue;
			}

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
			CompiledMaterialDefinition cmd = loadCompiledMaterialDefinition(jsonFile, true, args.raw, false);
			ByteBuf buf = new ByteBuf();
			cmd.saveTo(buf, args.encrypt ? EncryptionVariants.SimplePassphrase : EncryptionVariants.None);
			FileUtil.write(outputFile, buf.toByteArray());
		}
	}

	private static void compile(Args args) {
		fileLoop:
		for (String path : args.inputPath) {
			File inputFile = new File(path);
			if (!inputFile.exists()) {
				System.out.println("Error: input file does not exist");
				continue;
			}

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

			File tmp = new File(srcDir, "varying.def.sc");
			if (!varyingDefFile.exists() && tmp.exists())
				varyingDefFile = tmp;
			tmp = new File(srcDir, "vertex.sc");
			if (!vertexSourceFile.exists() && tmp.exists())
				vertexSourceFile = tmp;
			tmp = new File(srcDir, "fragment.sc");
			if (!fragmentSourceFile.exists() && tmp.exists())
				fragmentSourceFile = tmp;
			tmp = new File(srcDir, "compute.sc");
			if (!computeSourceFile.exists() && tmp.exists())
				computeSourceFile = tmp;

			File definesJsonFile = new File(parent, "defines.json");
			JsonObject materialDefines = null;
			JsonObject passDefines = null;
			JsonObject flagDefines = null;
			if (definesJsonFile.exists()) {
				JsonObject definesJson = JsonParser.parseString(FileUtil.readString(definesJsonFile)).getAsJsonObject();
				if (definesJson.has("material"))
					materialDefines = definesJson.getAsJsonObject("material");
				else if (definesJson.has("materials"))
					materialDefines = definesJson.getAsJsonObject("materials");

				if (definesJson.has("pass"))
					passDefines = definesJson.getAsJsonObject("pass");
				else if (definesJson.has("passes"))
					passDefines = definesJson.getAsJsonObject("passes");

				if (definesJson.has("flag"))
					flagDefines = definesJson.getAsJsonObject("flag");
				if (definesJson.has("flags"))
					flagDefines = definesJson.getAsJsonObject("flags");
			}

			String compilerPath = findCompilerPath(args.shaderCompilerPath);
			if (compilerPath == null) {
				System.out.println("Error: shaderc not found");
				return;
			}

			VaryingDefPreprocessor preprocessor = new VaryingDefPreprocessor(varyingDefFile);
			BgfxShaderCompiler compiler = new BgfxShaderCompiler(compilerPath);
			if (args.includePath != null) {
				for (String p : args.includePath) {
					compiler.addIncludePath(p);
				}
			}
			compiler.setDebug(args.debug);

			CompiledMaterialDefinition cmd = loadCompiledMaterialDefinition(jsonFile, false, args.raw, true);
			for (Map.Entry<String, CompiledMaterialDefinition.Pass> passEntry : cmd.passMap.entrySet()) {
				String passName = passEntry.getKey();
				System.out.println("Compiling " + passName);
				for (CompiledMaterialDefinition.Variant variant : passEntry.getValue().variantList) {
					for (Map.Entry<PlatformShaderStage, CompiledMaterialDefinition.ShaderCode> entry : variant.shaderCodeMap.entrySet()) {
						PlatformShaderStage platformShaderStage = entry.getKey();
						CompiledMaterialDefinition.ShaderCode shaderCode = entry.getValue();

						Defines defines = new Defines();
						defines.addDefine("BGFX_CONFIG_MAX_BONES", "4");

						if (materialDefines != null && materialDefines.has(cmd.name)) {
							for (JsonElement element : materialDefines.getAsJsonArray(cmd.name)) {
								defines.addDefine(element.getAsString());
							}
						}

						if (passDefines != null && passDefines.has(passName)) {
							for (JsonElement element : passDefines.getAsJsonArray(passName)) {
								defines.addDefine(element.getAsString());
							}
						} else {
							defines.addDefine(StringUtil.toUnderScore(passName));
						}

						for (Map.Entry<String, String> flag : variant.flags.entrySet()) {
							if (flagDefines != null && flagDefines.has(flag.getKey())) {
								JsonObject flagJson = flagDefines.getAsJsonObject(flag.getKey());
								if (flagJson.has(flag.getValue())) {
									for (JsonElement element : flagJson.getAsJsonArray(flag.getValue())) {
										defines.addDefine(element.getAsString());
									}
								}
							} else if ("On".equals(flag.getValue())) {
								defines.addDefine(StringUtil.toUnderScore(flag.getKey()));
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

						File varyingDef = preprocessor.getPreprocessedVaryingDef(platformShaderStage);
						if (varyingDef == null) {
							System.out.println("Failed to preprocess varyingdef");
							continue fileLoop;
						}

						byte[] compiled = compiler.compile(input, varyingDef, defines, platformShaderStage.platform, platformShaderStage.stage);
						if (compiled == null) {
							System.out.println("Compilation failure");
							continue fileLoop;
						}
						shaderCode.bgfxShaderData = compiled;
					}
				}
			}

			ByteBuf buf = new ByteBuf();
			cmd.saveTo(buf, args.encrypt ? EncryptionVariants.SimplePassphrase : EncryptionVariants.None);
			FileUtil.write(outputFile, buf.toByteArray());
		}
	}

	private static void mergeData(Args args) {
		if (args.inputPath.size() < 2) {
			return;
		}
		if (args.outputPath == null) {
			System.out.println("Error: output directory not specified");
			return;
		}
		File outputDir = new File(args.outputPath);
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			System.out.println("Error: failed to create output directory");
			return;
		}
		if (!outputDir.isDirectory()) {
			System.out.println("Error: output is not a directory");
			return;
		}

		String name = null;
		CompiledMaterialDefinition merged = null;
		for (String path : args.inputPath) {
			File inputFile = new File(path);
			if (!inputFile.exists()) {
				System.out.println("Error: input file does not exist");
				return;
			}
			if (!inputFile.isFile() || !inputFile.canRead()) {
				System.out.println("Error: input file not readable");
				return;
			}

			System.out.println("Merging " + inputFile.getAbsolutePath());

			JsonObject jsonObject = JsonParser.parseString(FileUtil.readString(inputFile)).getAsJsonObject();
			boolean dataOnly = jsonObject.has("dataOnly") && jsonObject.get("dataOnly").getAsBoolean();
			if (!dataOnly) {
				System.out.println("Error: not dataOnly");
				return;
			}

			CompiledMaterialDefinition cmd = loadDataOnlyJson(jsonObject);
			if (merged == null) {
				String fileName = inputFile.getName();
				name = fileName.substring(0, fileName.lastIndexOf(".json"));
				merged = cmd;
				continue;
			}
			if (merged.version == cmd.version
					&& merged.hasParentName == cmd.hasParentName
					&& merged.encryptionVariant == cmd.encryptionVariant
					&& Objects.equals(merged.name, cmd.name)
					&& Objects.equals(merged.parentName, cmd.parentName)
//					&& Objects.equals(merged.samplerDefinitionMap, cmd.samplerDefinitionMap)
					&& Objects.equals(merged.samplerDefinitionMap.keySet(), cmd.samplerDefinitionMap.keySet())
					&& Objects.equals(merged.propertyFieldMap, cmd.propertyFieldMap)
					&& merged.passMap != null && cmd.passMap != null
					&& merged.passMap.size() == cmd.passMap.size()) {

				for (Map.Entry<String, CompiledMaterialDefinition.Pass> passEntry : merged.passMap.entrySet()) {
					CompiledMaterialDefinition.Pass pass1 = passEntry.getValue();
					CompiledMaterialDefinition.Pass pass2 = cmd.passMap.get(passEntry.getKey());

					if (pass1.hasDefaultBlendMode == pass2.hasDefaultBlendMode
							&& Objects.equals(pass1.bitSet, pass2.bitSet)
							&& Objects.equals(pass1.fallback, pass2.fallback)
							&& pass1.defaultBlendMode == pass2.defaultBlendMode
							&& Objects.equals(pass1.flagDefaultValues, pass2.flagDefaultValues)
							&& pass1.variantList != null && pass2.variantList != null
							&& pass1.variantList.size() == pass2.variantList.size()) {
						for (CompiledMaterialDefinition.Variant variant1 : pass1.variantList) {
							boolean found = false;
							for (CompiledMaterialDefinition.Variant variant2 : pass2.variantList) {
								if (variant1.flags.equals(variant2.flags)) {
									variant1.shaderCodeMap.putAll(variant2.shaderCodeMap);
									found = true;
									break;
								}
							}
							if (!found) {
								System.out.println("Merge failure : no variant with the same flags found");
								return;
							}
						}
					} else {
						System.out.println("Merge failure: pass " + passEntry.getKey() + " not same");
						return;
					}
				}
			} else {
				System.out.println("Merge failure: CompiledMaterialDefinition not same");
				return;
			}
		}

		saveCompiledMaterialDefinition(merged, name, outputDir, false, false, true);
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

	public static void unpack(File inputFile, String outputDirPath, boolean addFlagsToCode, boolean raw, boolean dataOnly) {
		ByteBuf buf = new ByteBuf(FileUtil.readAllBytes(inputFile));
		CompiledMaterialDefinition cmd = new CompiledMaterialDefinition();
		cmd.loadFrom(buf);

		String fileName = inputFile.getName();
		String name = fileName.substring(0, fileName.indexOf(".material.bin"));
		File outputFile = outputDirPath != null ? new File(outputDirPath, name) : new File(inputFile.getParentFile(), name);
		saveCompiledMaterialDefinition(cmd, name, outputFile, addFlagsToCode, raw, dataOnly);
	}

	public static void saveCompiledMaterialDefinition(CompiledMaterialDefinition cmd, String name, File outputDir, boolean addFlagsToCode, boolean raw, boolean dataOnly) {
    	if (!outputDir.exists() && !outputDir.mkdirs())
    		return;

		JsonObject jsonObject = GSON.toJsonTree(cmd).getAsJsonObject();
		if (!dataOnly) {
			JsonArray passes = new JsonArray();
			for (Map.Entry<String, CompiledMaterialDefinition.Pass> entry : cmd.passMap.entrySet()) {
				String passName = entry.getKey();
				savePass(entry.getValue(), passName, new File(outputDir, passName), addFlagsToCode, raw);
				passes.add(passName);
			}
			jsonObject.add("passes", passes);
		} else {
			jsonObject.addProperty("dataOnly", true);
			JsonObject passMapJson = GSON.toJsonTree(cmd.passMap).getAsJsonObject();
			for (Map.Entry<String, CompiledMaterialDefinition.Pass> entry : cmd.passMap.entrySet()) {
				CompiledMaterialDefinition.Pass pass = entry.getValue();
				JsonObject passJson = passMapJson.get(entry.getKey()).getAsJsonObject();
				JsonArray variantListJson = passJson.getAsJsonArray("variantList");
				for (int i = 0; i < pass.variantList.size(); ++i) {
					CompiledMaterialDefinition.Variant variant = pass.variantList.get(i);
					JsonObject variantJson = variantListJson.get(i).getAsJsonObject();

					JsonArray shaderCodesJson = new JsonArray();
					for (Map.Entry<PlatformShaderStage, CompiledMaterialDefinition.ShaderCode> entry1 : variant.shaderCodeMap.entrySet()) {
						JsonObject entryJson = new JsonObject();

						PlatformShaderStage platformShaderStage = entry1.getKey();
						entryJson.add("platformShaderStage", GSON.toJsonTree(platformShaderStage));

						CompiledMaterialDefinition.ShaderCode shaderCode = entry1.getValue();
						entryJson.add("shaderCode", GSON.toJsonTree(shaderCode));

						shaderCodesJson.add(entryJson);
					}

					variantJson.add("shaderCodes", shaderCodesJson);
				}
			}

			jsonObject.add("passMap", passMapJson);
		}

		FileUtil.writeString(new File(outputDir, name + ".json"), GSON.toJson(jsonObject));
	}

	private static void savePass(CompiledMaterialDefinition.Pass pass, String passName, File outputDir, boolean addFlagsToCode, boolean raw) {
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
					if (addFlagsToCode && (platformShaderStage.platformName.startsWith("GLSL") || platformShaderStage.platformName.startsWith("ESSL") || platformShaderStage.platformName.startsWith("Metal"))) {
						StringBuilder sb = new StringBuilder();
						List<Map.Entry<String, String>> flagList = new ArrayList<>(variant.flags.entrySet());
						flagList.sort(Map.Entry.comparingByKey());
						for (Map.Entry<String, String> flag : flagList) {
							sb.append("//");
							sb.append(flag.getKey()).append("=").append(flag.getValue());
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

	public static CompiledMaterialDefinition loadCompiledMaterialDefinition(File jsonFile, boolean loadCode, boolean raw, boolean canDataOnly) {
		File inputDir = jsonFile.getParentFile();
		JsonObject jsonObject = JsonParser.parseString(FileUtil.readString(jsonFile)).getAsJsonObject();

		boolean dataOnly = jsonObject.has("dataOnly") && jsonObject.get("dataOnly").getAsBoolean();
		if (dataOnly && !canDataOnly) {
			throw new RuntimeException("Cannot be dataOnly");
		}

		if (!dataOnly) {
			CompiledMaterialDefinition cmd = GSON.fromJson(jsonObject, CompiledMaterialDefinition.class);
			cmd.passMap = new LinkedHashMap<>();
			JsonArray passes = jsonObject.get("passes").getAsJsonArray();
			for (int i = 0; i < passes.size(); ++i) {
				String passName = passes.get(i).getAsString();
				cmd.passMap.put(passName, loadPass(new File(inputDir, passName + File.separator + passName + ".json"), loadCode, raw));
			}
			return cmd;
		} else {
			return loadDataOnlyJson(jsonObject);
		}
	}

	private static CompiledMaterialDefinition loadDataOnlyJson(JsonObject json) {
		CompiledMaterialDefinition cmd = GSON.fromJson(json, CompiledMaterialDefinition.class);
		cmd.passMap = new LinkedHashMap<>();
		JsonObject passMap = json.get("passMap").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : passMap.entrySet()) {
			CompiledMaterialDefinition.Pass pass = GSON.fromJson(entry.getValue(), CompiledMaterialDefinition.Pass.class);

			JsonArray variantList = entry.getValue().getAsJsonObject().get("variantList").getAsJsonArray();
			for (int i = 0; i < pass.variantList.size(); ++i) {
				CompiledMaterialDefinition.Variant variant = pass.variantList.get(i);
				variant.shaderCodeMap = new LinkedHashMap<>();

				JsonArray shaderCodes = variantList.get(i).getAsJsonObject().get("shaderCodes").getAsJsonArray();
				for (int j = 0; j < shaderCodes.size(); ++j) {
					JsonObject entryJson = shaderCodes.get(j).getAsJsonObject();

					PlatformShaderStage platformShaderStage = GSON.fromJson(entryJson.get("platformShaderStage"), PlatformShaderStage.class);
					CompiledMaterialDefinition.ShaderCode shaderCode = GSON.fromJson(entryJson.get("shaderCode"), CompiledMaterialDefinition.ShaderCode.class);

					variant.shaderCodeMap.put(platformShaderStage, shaderCode);
				}
			}

			cmd.passMap.put(entry.getKey(), pass);
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
