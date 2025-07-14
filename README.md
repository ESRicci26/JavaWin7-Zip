# JavaWin7-Zip

Uma aplicação Java Swing moderna para compactação e descompactação de arquivos, semelhante ao 7-Zip.

## 🚀 Funcionalidades

- **Compactação**: Compacte múltiplos arquivos e pastas em formato ZIP
- **Descompactação**: Descompacte arquivos nos formatos ZIP, 7Z, TAR, TAR.GZ, GZ
- **Interface Intuitiva**: Interface gráfica moderna com Swing e FlatLaf
- **Seleção Múltipla**: Selecione múltiplos arquivos e pastas com file chooser
- **Progresso em Tempo Real**: Barra de progresso e status durante as operações
- **Abertura Automática**: Opção para abrir a pasta de destino após a operação
- **Proteção de Segurança**: Proteção contra zip slip attacks
- **Suporte a Timestamps**: Preserva datas de modificação dos arquivos

## 📋 Requisitos

- Java JDK 11 ou superior
- Apache Maven 3.6 ou superior
- Sistema Operacional: Windows, Linux ou macOS

## 🛠️ Instalação e Execução

### 1. Clonar ou baixar o projeto

```bash
git clone <url-do-repositorio>
cd JavaWinZip
```

### 2. Compilar o projeto

```bash
mvn clean compile
```

### 3. Executar a aplicação

#### Windows:
```bash
run.bat
```

#### Linux/Mac:
```bash
chmod +x run.sh
./run.sh
```

#### Ou manualmente:
```bash
mvn exec:java -Dexec.mainClass="com.javawinzip.JavaWinZipApp"
```

## 📦 Criar Executável

Para criar um JAR executável:

```bash
mvn clean package
```

O arquivo JAR será criado em `target/JavaWinZip-1.0.0.jar`

Para executar o JAR:

```bash
java -jar target/JavaWinZip-1.0.0.jar
```

## 🎯 Como Usar

### Compactação
1. Clique em "Adicionar Arquivos" para selecionar arquivos individuais
2. Clique em "Adicionar Pastas" para selecionar pastas inteiras
3. Clique em "Compactar" e escolha o local para salvar o arquivo ZIP
4. Aguarde a conclusão da operação

### Descompactação
1. Clique em "Descompactar"
2. Selecione o arquivo compactado (ZIP, 7Z, TAR, etc.)
3. Escolha a pasta de destino
4. Aguarde a conclusão da operação

### Outras Funcionalidades
- **Limpar Lista**: Remove todos os arquivos da lista
- **Progresso**: Acompanhe o progresso das operações na barra inferior
- **Status**: Veja informações sobre a operação atual

## 🏗️ Estrutura do Projeto

```
JavaWinZip/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── javawinzip/
│                   ├── JavaWinZipApp.java          # Classe principal
│                   ├── gui/
│                   │   └── MainFrame.java          # Interface principal
│                   ├── services/
│                   │   ├── CompressionService.java # Serviço de compactação
│                   │   └── DecompressionService.java # Serviço de descompactação
│                   └── utils/
│                       └── FileUtils.java          # Utilitários de arquivo
├── pom.xml                                         # Configuração Maven
├── run.bat                                         # Script Windows
├── run.sh                                          # Script Linux/Mac
└── README.md                                       # Documentação
```

## 📚 Dependências

- **Apache Commons Compress**: Para suporte a múltiplos formatos de compactação
- **XZ Utils**: Para suporte ao formato XZ
- **FlatLaf**: Para interface moderna e responsiva

## 🔧 Formatos Suportados

### Compactação
- ZIP (com diferentes níveis de compressão)

### Descompactação
- ZIP
- 7Z
- TAR
- TAR.GZ / TGZ
- GZ

## 🛡️ Segurança

A aplicação inclui proteções contra:
- **Zip Slip Attack**: Validação de caminhos de destino
- **Path Traversal**: Verificação de caminhos seguros
- **Arquivos Maliciosos**: Validação de nomes de arquivo

## 🎨 Interface

A aplicação utiliza:
- **FlatLaf**:

- ## 📞 Contact: (011) 98678-2984

Name: esricci26@gmail.com

Project Link: https://github.com/ESRicci26/JavaWin7-Zip
