package javaricci.com.br;

//package com.javawinzip.utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilitários para manipulação de arquivos
 */
public class FileUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Abre a localização de um arquivo no explorador do sistema
     */
    public static void openFileLocation(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                
                if (file.isFile()) {
                    // Se for um arquivo, abrir o diretório pai e selecionar o arquivo
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        Runtime.getRuntime().exec("explorer.exe /select," + file.getAbsolutePath());
                    } else {
                        desktop.open(file.getParentFile());
                    }
                } else {
                    // Se for um diretório, abrir diretamente
                    desktop.open(file);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao abrir localização do arquivo: " + e.getMessage());
        }
    }
    
    /**
     * Formata o tamanho do arquivo em formato legível
     */
    public static String formatFileSize(long size) {
        if (size < 0) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Formata a data de modificação do arquivo
     */
    public static String formatFileDate(long timestamp) {
        if (timestamp <= 0) return "Data desconhecida";
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Obtém a extensão de um arquivo
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        return lastDotIndex > 0 ? name.substring(lastDotIndex + 1).toLowerCase() : "";
    }
    
    /**
     * Verifica se um arquivo é um arquivo compactado suportado
     */
    public static boolean isCompressedFile(File file) {
        String extension = getFileExtension(file);
        return extension.equals("zip") || 
               extension.equals("7z") || 
               extension.equals("tar") || 
               extension.equals("gz") || 
               extension.equals("tgz") ||
               extension.equals("rar");
    }
    
    /**
     * Obtém o ícone apropriado para um tipo de arquivo
     */
    public static String getFileIcon(File file) {
        if (file.isDirectory()) {
            return "📁";
        }
        
        String extension = getFileExtension(file);
        switch (extension) {
            case "zip":
            case "7z":
            case "tar":
            case "gz":
            case "tgz":
            case "rar":
                return "📦";
            case "txt":
            case "log":
                return "📄";
            case "pdf":
                return "📋";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "🖼️";
            case "mp3":
            case "wav":
            case "flac":
                return "🎵";
            case "mp4":
            case "avi":
            case "mov":
            case "mkv":
                return "🎬";
            case "exe":
            case "msi":
                return "⚙️";
            case "java":
            case "js":
            case "py":
            case "cpp":
            case "c":
                return "💻";
            default:
                return "📄";
        }
    }
    
    /**
     * Cria um nome de arquivo único se já existir
     */
    public static File createUniqueFile(File file) {
        if (!file.exists()) {
            return file;
        }
        
        String name = file.getName();
        String baseName;
        String extension = "";
        
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex > 0) {
            baseName = name.substring(0, lastDotIndex);
            extension = name.substring(lastDotIndex);
        } else {
            baseName = name;
        }
        
        int counter = 1;
        File uniqueFile;
        
        do {
            String newName = baseName + " (" + counter + ")" + extension;
            uniqueFile = new File(file.getParent(), newName);
            counter++;
        } while (uniqueFile.exists());
        
        return uniqueFile;
    }
    
    /**
     * Verifica se um diretório está vazio
     */
    public static boolean isDirectoryEmpty(File directory) {
        if (!directory.isDirectory()) {
            return false;
        }
        
        try {
            return Files.list(directory.toPath()).findAny().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Conta o número total de arquivos em um diretório recursivamente
     */
    public static long countFilesInDirectory(File directory) {
        if (!directory.isDirectory()) {
            return 0;
        }
        
        try {
            return Files.walk(directory.toPath())
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * Obtém o tamanho total de um diretório
     */
    public static long getDirectorySize(File directory) {
        if (!directory.isDirectory()) {
            return 0;
        }
        
        try {
            return Files.walk(directory.toPath())
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * Valida se um nome de arquivo é válido
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        // Caracteres inválidos no Windows
        String invalidChars = "<>:\"/\\|?*";
        for (char c : invalidChars.toCharArray()) {
            if (fileName.indexOf(c) != -1) {
                return false;
            }
        }
        
        // Nomes reservados no Windows
        String[] reservedNames = {
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        };
        
        String upperName = fileName.toUpperCase();
        for (String reserved : reservedNames) {
            if (upperName.equals(reserved) || upperName.startsWith(reserved + ".")) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Cria um backup de um arquivo
     */
    public static File createBackup(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Arquivo não existe: " + file.getAbsolutePath());
        }
        
        String baseName = file.getName();
        String extension = "";
        
        int lastDotIndex = baseName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = baseName.substring(lastDotIndex);
            baseName = baseName.substring(0, lastDotIndex);
        }
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupName = baseName + "_backup_" + timestamp + extension;
        
        File backupFile = new File(file.getParent(), backupName);
        Files.copy(file.toPath(), backupFile.toPath());
        
        return backupFile;
    }
}