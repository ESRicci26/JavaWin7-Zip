package javaricci.com.br;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.Deflater;

/**
 * Serviço responsável pela compactação de arquivos e pastas
 */
public class CompressionService {
    
    /**
     * Interface para callback de progresso
     */
    public interface ProgressCallback {
        void onProgress(int progress, String message);
    }
    
    /**
     * Compacta um array de arquivos e pastas em um arquivo ZIP
     * 
     * @param files Array de arquivos e pastas para compactar
     * @param outputFile Arquivo ZIP de saída
     * @param callback Callback para atualização do progresso
     * @throws IOException Se ocorrer erro durante a compactação
     */
    public void compressFiles(File[] files, File outputFile, ProgressCallback callback) throws IOException {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("Nenhum arquivo ou pasta especificado");
        }
        
        // Calcular número total de arquivos para o progresso
        int totalFiles = countFiles(files);
        final int[] processedFiles = {0};
        
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {
            
            // Configurar compressão
            zos.setLevel(Deflater.DEFAULT_COMPRESSION);
            zos.setMethod(ZipArchiveOutputStream.DEFLATED);
            
            callback.onProgress(0, "Iniciando compactação...");
            
            for (File file : files) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        addDirectoryToZip(file, file.getName(), zos, callback, totalFiles, processedFiles);
                    } else {
                        addFileToZip(file, file.getName(), zos, callback, totalFiles, processedFiles);
                    }
                }
            }
            
            callback.onProgress(100, "Finalizando arquivo...");
            zos.finish();
        }
        
        callback.onProgress(100, "Compactação concluída!");
    }
    
    /**
     * Adiciona um arquivo ao ZIP
     */
    private void addFileToZip(File file, String entryName, ZipArchiveOutputStream zos, 
                             ProgressCallback callback, int totalFiles, int[] processedFiles) throws IOException {
        
        callback.onProgress(
            (processedFiles[0] * 100) / totalFiles,
            "Compactando: " + file.getName()
        );
        
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        entry.setSize(file.length());
        entry.setTime(file.lastModified());
        zos.putArchiveEntry(entry);
        
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }
        
        zos.closeArchiveEntry();
        processedFiles[0]++;
    }
    
    /**
     * Adiciona uma pasta ao ZIP recursivamente
     */
    private void addDirectoryToZip(File dir, String baseName, ZipArchiveOutputStream zos,
                                  ProgressCallback callback, int totalFiles, int[] processedFiles) throws IOException {
        
        Path dirPath = dir.toPath();
        
        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String relativePath = baseName + "/" + dirPath.relativize(file).toString().replace('\\', '/');
                addFileToZip(file.toFile(), relativePath, zos, callback, totalFiles, processedFiles);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.equals(dirPath)) {
                    String relativePath = baseName + "/" + dirPath.relativize(dir).toString().replace('\\', '/') + "/";
                    
                    ZipArchiveEntry entry = new ZipArchiveEntry(relativePath);
                    entry.setTime(dir.toFile().lastModified());
                    zos.putArchiveEntry(entry);
                    zos.closeArchiveEntry();
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println("Erro ao processar arquivo: " + file + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Conta o número total de arquivos para calcular o progresso
     */
    private int countFiles(File[] files) {
        int count = 0;
        for (File file : files) {
            if (file.exists()) {
                if (file.isDirectory()) {
                    count += countFilesInDirectory(file);
                } else {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Conta arquivos em uma pasta recursivamente
     */
    private int countFilesInDirectory(File dir) {
        int count = 0;
        try {
            count = (int) Files.walk(dir.toPath())
                .filter(Files::isRegularFile)
                .count();
        } catch (IOException e) {
            System.err.println("Erro ao contar arquivos em: " + dir + " - " + e.getMessage());
        }
        return count;
    }
}