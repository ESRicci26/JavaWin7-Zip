package javaricci.com.br;


import javaricci.com.br.CompressionService;
import javaricci.com.br.DecompressionService;
import javaricci.com.br.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Janela principal da aplicação JavaWinZip
 */
public class MainFrame extends JFrame {
    
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton compressButton;
    private JButton decompressButton;
    private JButton addFilesButton;
    private JButton addFoldersButton;
    private JButton clearButton;
    
    private CompressionService compressionService;
    private DecompressionService decompressionService;
    
    public MainFrame() {
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        compressionService = new CompressionService();
        decompressionService = new DecompressionService();
    }
    
    private void initComponents() {
        setTitle("JavaWinZip - Compactador de Arquivos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Ícone da aplicação
        try {
            setIconImage(createIcon());
        } catch (Exception e) {
            System.err.println("Erro ao definir ícone: " + e.getMessage());
        }
        
        // Lista de arquivos
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setCellRenderer(new FileListCellRenderer());
        
        // Botões
        addFilesButton = new JButton("Adicionar Arquivos");
        addFilesButton.setIcon(createButtonIcon("📄"));
        
        addFoldersButton = new JButton("Adicionar Pastas");
        addFoldersButton.setIcon(createButtonIcon("📁"));
        
        compressButton = new JButton("Compactar");
        compressButton.setIcon(createButtonIcon("📦"));
        compressButton.setEnabled(false);
        
        decompressButton = new JButton("Descompactar");
        decompressButton.setIcon(createButtonIcon("📂"));
        
        clearButton = new JButton("Limpar Lista");
        clearButton.setIcon(createButtonIcon("🗑️"));
        
        // Barra de progresso
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Pronto");
        
        // Status
        statusLabel = new JLabel("Pronto para usar");
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel superior com botões
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(addFilesButton);
        topPanel.add(addFoldersButton);
        topPanel.add(new JSeparator(SwingConstants.VERTICAL));
        topPanel.add(compressButton);
        topPanel.add(decompressButton);
        topPanel.add(new JSeparator(SwingConstants.VERTICAL));
        topPanel.add(clearButton);
        
        // Painel central com lista de arquivos
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Arquivos e Pastas"));
        
        // Painel inferior com progresso e status
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addFilesButton.addActionListener(e -> addFiles());
        addFoldersButton.addActionListener(e -> addFolders());
        compressButton.addActionListener(e -> compressFiles());
        decompressButton.addActionListener(e -> decompressFile());
        clearButton.addActionListener(e -> clearList());
        
        // Atualizar botões quando a lista mudar
        listModel.addListDataListener(new javax.swing.event.ListDataListener() {
            @Override
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                updateButtons();
            }
            
            @Override
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                updateButtons();
            }
            
            @Override
            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                updateButtons();
            }
        });
    }
    
    private void addFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Selecionar Arquivos");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                if (!listModel.contains(file.getAbsolutePath())) {
                    listModel.addElement(file.getAbsolutePath());
                }
            }
            updateStatus("Adicionados " + files.length + " arquivo(s)");
        }
    }
    
    private void addFolders() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Selecionar Pastas");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();
            if (!listModel.contains(folder.getAbsolutePath())) {
                listModel.addElement(folder.getAbsolutePath());
                updateStatus("Adicionada pasta: " + folder.getName());
            }
        }
    }
    
    private void compressFiles() {
        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Adicione arquivos ou pastas para compactar", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Arquivo Compactado");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Arquivo ZIP (*.zip)", "zip"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();
            if (!outputFile.getName().toLowerCase().endsWith(".zip")) {
                outputFile = new File(outputFile.getAbsolutePath() + ".zip");
            }
            
            // Verificar se o arquivo já existe
            if (outputFile.exists()) {
                int result = JOptionPane.showConfirmDialog(this,
                    "O arquivo já existe. Deseja substituí-lo?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            compressFilesAsync(outputFile);
        }
    }
    
    private void compressFilesAsync(File outputFile) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                setButtonsEnabled(false);
                progressBar.setIndeterminate(true);
                
                publish("Iniciando compactação...");
                
                // Converter lista para array de arquivos
                File[] files = new File[listModel.size()];
                for (int i = 0; i < listModel.size(); i++) {
                    files[i] = new File(listModel.get(i));
                }
                
                compressionService.compressFiles(files, outputFile, 
                    (progress, message) -> {
                        if (progress >= 0) {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(progress);
                        }
                        publish(message);
                    });
                
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    updateStatus(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    updateStatus("Compactação concluída com sucesso!");
                    
                    int result = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Compactação concluída! Deseja abrir a pasta de destino?",
                        "Sucesso", JOptionPane.YES_NO_OPTION);
                    
                    if (result == JOptionPane.YES_OPTION) {
                        FileUtils.openFileLocation(outputFile);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Erro durante a compactação: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    updateStatus("Erro na compactação");
                } finally {
                    setButtonsEnabled(true);
                    progressBar.setValue(0);
                    progressBar.setString("Pronto");
                }
            }
        };
        
        worker.execute();
    }
    
    private void decompressFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivo para Descompactar");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Arquivos Compactados (*.zip, *.7z, *.tar, *.gz)", 
            "zip", "7z", "tar", "gz", "rar"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File inputFile = fileChooser.getSelectedFile();
            
            // Selecionar pasta de destino
            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setDialogTitle("Selecionar Pasta de Destino");
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            if (folderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File outputFolder = folderChooser.getSelectedFile();
                decompressFileAsync(inputFile, outputFolder);
            }
        }
    }
    
    private void decompressFileAsync(File inputFile, File outputFolder) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                setButtonsEnabled(false);
                progressBar.setIndeterminate(true);
                
                publish("Iniciando descompactação...");
                
                decompressionService.decompressFile(inputFile, outputFolder,
                    (progress, message) -> {
                        if (progress >= 0) {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(progress);
                        }
                        publish(message);
                    });
                
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    updateStatus(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    updateStatus("Descompactação concluída com sucesso!");
                    
                    int result = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Descompactação concluída! Deseja abrir a pasta de destino?",
                        "Sucesso", JOptionPane.YES_NO_OPTION);
                    
                    if (result == JOptionPane.YES_OPTION) {
                        FileUtils.openFileLocation(outputFolder);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Erro durante a descompactação: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    updateStatus("Erro na descompactação");
                } finally {
                    setButtonsEnabled(true);
                    progressBar.setValue(0);
                    progressBar.setString("Pronto");
                }
            }
        };
        
        worker.execute();
    }
    
    private void clearList() {
        listModel.clear();
        updateStatus("Lista limpa");
    }
    
    private void updateButtons() {
        compressButton.setEnabled(!listModel.isEmpty());
        clearButton.setEnabled(!listModel.isEmpty());
    }
    
    private void setButtonsEnabled(boolean enabled) {
        addFilesButton.setEnabled(enabled);
        addFoldersButton.setEnabled(enabled);
        compressButton.setEnabled(enabled && !listModel.isEmpty());
        decompressButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
        progressBar.setString(message);
    }
    
    private Image createIcon() {
        // Criar um ícone simples para a aplicação
        Image img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRoundRect(4, 4, 24, 24, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Z", 12, 22);
        g2d.dispose();
        return img;
    }
    
    private Icon createButtonIcon(String emoji) {
        return new ImageIcon(createEmojiIcon(emoji));
    }
    
    private Image createEmojiIcon(String emoji) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (16 - fm.stringWidth(emoji)) / 2;
        int y = (16 - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(emoji, x, y);
        g2d.dispose();
        return img;
    }
    
    /**
     * Renderer customizado para a lista de arquivos
     */
    private class FileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value != null) {
                File file = new File(value.toString());
                setText(file.getName() + " (" + file.getAbsolutePath() + ")");
                
                if (file.isDirectory()) {
                    setIcon(createButtonIcon("📁"));
                } else {
                    setIcon(createButtonIcon("📄"));
                }
            }
            
            return this;
        }
    }
}