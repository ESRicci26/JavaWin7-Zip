package javaricci.com.br;

//package com.javawinzip.services;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Serviço responsável pela descompactação de arquivos
 */
public class DecompressionService {
    
    /**
     * Interface para callback de progresso
     */
    public interface ProgressCallback {
        void onProgress(int progress, String message);
    }
    
    /**
     * Descompacta um arquivo para uma pasta de destino
     * 
     * @param inputFile Arquivo compactado
     * @param outputFolder Pasta de destino
     * @param callback Callback para atualização do progresso
     * @throws IOException Se ocorrer erro durante a descompactação
     */
    public void decompressFile(File inputFile, File outputFolder, ProgressCallback callback) throws IOException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + inputFile.getAbsolutePath());
        }
        
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        
        String fileName = inputFile.getName().toLowerCase();
        
        callback.onProgress(0, "Analisando arquivo...");
        
        if (fileName.endsWith(".zip")) {
            decompressZip(inputFile, outputFolder, callback);
        } else if (fileName.endsWith(".7z")) {
            decompress7z(inputFile, outputFolder, callback);
        } else if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")) {
            decompressTarGz(inputFile, outputFolder, callback);
        } else if (fileName.endsWith(".tar")) {
            decompressTar(inputFile, outputFolder, callback);
        } else if (fileName.endsWith(".gz")) {
            decompressGz(inputFile, outputFolder, callback);
        } else {
            throw new UnsupportedOperationException("Formato de arquivo não suportado: " + fileName);
        }
    }
    
    /**
     * Descompacta arquivo ZIP
     */
    private void decompressZip(File inputFile, File outputFolder, ProgressCallback callback) throws IOException {
        long totalSize = inputFile.length();
        long processedSize = 0;
        
        try (FileInputStream fis = new FileInputStream(inputFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                callback.onProgress(
                    (int) ((processedSize * 100) / totalSize),
                    "Extraindo: " + entryName
                );
                
                File destFile = new File(outputFolder, entryName);
                
                // Verificar se o caminho é seguro (evitar zip slip)
                if (!isValidDestination(destFile, outputFolder)) {
                    throw new IOException("Entrada inválida: " + entryName);
                }
                
                if (entry.isDirectory()) {
                    destFile.mkdirs();
                } else {
                    // Criar diretórios pais se necessário
                    destFile.getParentFile().mkdirs();
                    
                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    // Preservar timestamp
                    if (entry.getTime() != -1) {
                        destFile.setLastModified(entry.getTime());
                    }
                }
                
                processedSize += entry.getCompressedSize();
                zis.closeEntry();
            }
        }
        
        callback.onProgress(100, "Descompactação ZIP concluída!");
    }
    
    /**
     * Descompacta arquivo 7z
     */
    private void decompress7z(File inputFile, File outputFolder, ProgressCallback callback) throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(inputFile)) {
            SevenZArchiveEntry entry;
            byte[] buffer = new byte[8192];
            int entryCount = 0;
            
            while ((entry = sevenZFile.getNextEntry()) != null) {
                entryCount++;
                String entryName = entry.getName();
                callback.onProgress(-1, "Extraindo: " + entryName);
                
                File destFile = new File(outputFolder, entryName);
                
                if (!isValidDestination(destFile, outputFolder)) {
                    throw new IOException("Entrada inválida: " + entryName);
                }
                
                if (entry.isDirectory()) {
                    destFile.mkdirs();
                } else {
                    destFile.getParentFile().mkdirs();
                    
                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        int bytesRead;
                        while ((bytesRead = sevenZFile.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    if (entry.getLastModifiedDate() != null) {
                        destFile.setLastModified(entry.getLastModifiedDate().getTime());
                    }
                }
            }
            
            callback.onProgress(100, "Descompactação 7z concluída! " + entryCount + " arquivos extraídos.");
        }
    }
    
    /**
     * Descompacta arquivo TAR.GZ
     */
    private void decompressTarGz(File inputFile, File outputFolder, ProgressCallback callback) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             GzipCompressorInputStream gzis = new GzipCompressorInputStream(fis);
             TarArchiveInputStream tais = new TarArchiveInputStream(gzis)) {
            
            decompressTarStream(tais, outputFolder, callback, "TAR.GZ");
        }
    }
    
    /**
     * Descompacta arquivo TAR
     */
    private void decompressTar(File inputFile, File outputFolder, ProgressCallback callback) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             TarArchiveInputStream tais = new TarArchiveInputStream(fis)) {
            
            decompressTarStream(tais, outputFolder, callback, "TAR");
        }
    }
    
    /**
     * Descompacta stream TAR
     */
    private void decompressTarStream(TarArchiveInputStream tais, File outputFolder, 
                                   ProgressCallback callback, String format) throws IOException {
        ArchiveEntry entry;
        byte[] buffer = new byte[8192];
        int entryCount = 0;
        
        while ((entry = tais.getNextEntry()) != null) {
            entryCount++;
            String entryName = entry.getName();
            callback.onProgress(-1, "Extraindo: " + entryName);
            
            File destFile = new File(outputFolder, entryName);
            
            if (!isValidDestination(destFile, outputFolder)) {
                throw new IOException("Entrada inválida: " + entryName);
            }
            
            if (entry.isDirectory()) {
                destFile.mkdirs();
            } else {
                destFile.getParentFile().mkdirs();
                
                try (FileOutputStream fos = new FileOutputStream(destFile)) {
                    int bytesRead;
                    while ((bytesRead = tais.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                
                if (entry.getLastModifiedDate() != null) {
                    destFile.setLastModified(entry.getLastModifiedDate().getTime());
                }
            }
        }
        
        callback.onProgress(100, "Descompactação " + format + " concluída! " + entryCount + " arquivos extraídos.");
    }
    
    /**
     * Descompacta arquivo GZ
     */
    private void decompressGz(File inputFile, File outputFolder, ProgressCallback callback) throws IOException {
        String fileName = inputFile.getName();
        String outputName = fileName.substring(0, fileName.lastIndexOf('.'));
        File outputFile = new File(outputFolder, outputName);
        
        callback.onProgress(0, "Descompactando arquivo GZ...");
        
        try (FileInputStream fis = new FileInputStream(inputFile);
             GzipCompressorInputStream gzis = new GzipCompressorInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;
            long fileSize = inputFile.length();
            
            while ((bytesRead = gzis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                
                if (fileSize > 0) {
                    int progress = (int) ((totalRead * 100) / fileSize);
                    callback.onProgress(progress, "Descompactando: " + outputName);
                }
            }
        }
        
        callback.onProgress(100, "Descompactação GZ concluída!");
    }
    
    /**
     * Verifica se o destino é válido (proteção contra zip slip)
     */
    private boolean isValidDestination(File destFile, File outputFolder) {
        try {
            Path destPath = Paths.get(destFile.getCanonicalPath());
            Path outputPath = Paths.get(outputFolder.getCanonicalPath());
            return destPath.startsWith(outputPath);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Obtém informações sobre um arquivo compactado
     */
    public String getArchiveInfo(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + file.getAbsolutePath());
        }
        
        String fileName = file.getName().toLowerCase();
        StringBuilder info = new StringBuilder();
        
        info.append("Arquivo: ").append(file.getName()).append("\n");
        info.append("Tamanho: ").append(formatFileSize(file.length())).append("\n");
        info.append("Tipo: ");
        
        if (fileName.endsWith(".zip")) {
            info.append("ZIP");
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
                int entryCount = 0;
                while (zis.getNextEntry() != null) {
                    entryCount++;
                }
                info.append("\nArquivos: ").append(entryCount);
            }
        } else if (fileName.endsWith(".7z")) {
            info.append("7-Zip");
            try (SevenZFile sevenZFile = new SevenZFile(file)) {
                int entryCount = 0;
                while (sevenZFile.getNextEntry() != null) {
                    entryCount++;
                }
                info.append("\nArquivos: ").append(entryCount);
            }
        } else if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")) {
            info.append("TAR.GZ");
        } else if (fileName.endsWith(".tar")) {
            info.append("TAR");
        } else if (fileName.endsWith(".gz")) {
            info.append("GZIP");
        } else {
            info.append("Desconhecido");
        }
        
        return info.toString();
    }
    
    /**
     * Formata o tamanho do arquivo em formato legível
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}