package com.ddf.materialbintool;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

public class Main {
	public static Path outputDir;
	public static Path currentDir;

    public static void main(String[] args) throws IOException {
    	if (args.length <= 0)
    		return;
		Path inputPath = Paths.get(args[0]);
		outputDir = inputPath.resolve("out");
		if (!Files.exists(outputDir))
			Files.createDirectories(outputDir);

		Files.list(inputPath)
				.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".material.bin"))
				.forEach(path -> {
					String fileName = path.getFileName().toString();
					currentDir = outputDir.resolve(fileName.substring(0, fileName.indexOf(".material.bin")));
					try {
						if (!Files.exists(currentDir))
							Files.createDirectories(currentDir);
						byte[] bytes = Files.readAllBytes(path);
						CompiledMaterialDefinition cmd = new CompiledMaterialDefinition();
						cmd.loadFrom(bytes);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
    }

	public static void saveFile(String name, String data) {
    	saveFile(name, data.getBytes(StandardCharsets.UTF_8));
	}

	public static void saveFile(String name, byte[] data) {
    	Path path = currentDir.resolve(name);
		try {
			Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Writer openFile(String name) {
		Path path = currentDir.resolve(name);
		try {
			return new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
