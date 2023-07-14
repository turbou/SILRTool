# SILRTool
English | [Japanese](https://github.com/turbou/SILRTool/blob/main/README.ja.md)

`SILRTool` is a tool to help users who use Contrast.  
It is necessary to add a layer and environment variables to the target function when performing serverless IAST analysis. This tool will help you do that.

## System Requirements
#### OS
- Windows8.1, 10, 11
- macOS 11(Big Sur) or higher
#### Runtime  
- jre1.8

## Installation
There are several binaries on [Release](https://github.com/turbou/SILRTool/releases/latest) page. You can use SILRTool without build process.  
#### Windows
- SILRTool_X.X.X.zip  
  If you are downloading for the first time, please download this binary.  
  As jre folder(1.8.0_202) is included, you can use `SILRTool` without installing JRE.  
- SILRTool_X.X.X.exe  
  If you have already downloaded the zip, you can update SILRTool just by replacing the exe file.
#### macOS
- SILRTool_X.X.X.cli7z  
  Unzip this file with the command below.  
  ```bash
  # Installing p7zip
  brew install p7zip
  # Unzip command
  7z x SILRTool_X.X.X.cli7z
  ```

[^1]:Except for some environments.

## Prepare (For Your PC)
Please create a AWS profile(following files) by `aws configure` or by hand.    
- ~/.aws/config  
  specify region and format.
- ~/.aws/credentials  
  Authentication info(token, secretkey)

*Installing the AWS Cli is not required. If there is the above profile, it will work.*

## Settings
1. Set the Region, Layer Arn and Environment in `General` preference page.

## Usage
1. Execute Load Function  
2. Check the target function
3. Execute Add Layer or Rmv Layer  

## Building of SILRTool
### Building with build.gradle
Modify the following line of `build.gradle` according to your environment.
#### Windows 64bit
```gradle
compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```
#### Windows 32bit
```gradle
//compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```
#### macOS
```gradle
//compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```

### Build and Export Jar
#### Windows
```powershell
gradlew clean jar
```
#### macOS
```bash
./gradlew clean jar
```
jar file will be created under `build\libs`.

### Build and Run on Eclipse IDE
#### Windows
```powershell
gradlew cleanEclipse eclipse
```
#### macOS
```bash
./gradlew cleanEclipse eclipse
```
Refresh your project in Eclipse. Clean and Build if necessary.  
Specify `com.contrastsecurity.csvdltool.Main` class and execute Java. SILRTool will launch.

## Distribution
### Convert jar to executable binary
#### Windows (jar to exe)
- Using [launch4j](https://launch4j.sourceforge.net/).
- We prepare ready-to-use configuration file for launch4j.  
  - Correct the path according to your environment.  
  - Setting to bundle a jre folder to exe file is already enabled.  

#### macOS (jar to app)
1. Using javapackager.
1. If bundle jre, place the jre folder anywhere on the file system.
1. If needed, correct lines 3 to 7 in jarpackage.sh.
1. run `jarpackage.sh`.  
    ```bash
    ./jarpackage.sh
    ```
    app folder will be created under `build\libs/bundle`.

### Digital Signature
First of all, get the certificate file(pfx) and the certificate password.    
#### Windows
- Check arias
  ```powershell
  keytool -list -v -storetype pkcs12 -keystore C:\Users\turbou\Desktop\SILRTool_work\XXXXX.pfx
  # Enter a certificate password.
  ```
- Sign  
  Using sign4j in launch4j.  
  ```powershell
  cd C:\Program Files (x86)\launch4j\sign4j
  sign4j.exe java -jar jsign-2.0.jar --alias 1 --keystore C:\Users\turbou\Desktop\SILRTool_work\XXXXX.pfx --storepass [] C:\Users\turbou\Desktop\SILRTool_work\common\SILRTool_1.0.0.exe
  ```
- Confirm Digital Signatures  
  You can confirm the signature in the properties of the exe file.
#### macOS
- Install certifiate to your PC.  
  Double-click the pfx file to install into KeychainAccess. At that time, certificate password is needed.  
  After installation, copy the `Common Name`.
- Sign  
  Using codesign.  
  ```bash
  codesign --deep -s "Contrast Security, Inc." -v SILRTool_1.0.0.app
  ```
- Confirm Digital Signatures
  ```bash
  codesign -d --verbose=4 SILRTool_1.0.0.app
  ```
    
#### How to compress
- Windows  
  Using 7-Zip.
- macOS
  ```bash
  # Installing p7zip
  brew install p7zip
  # Compress
  7z a SILRTool_1.0.0.cli7z SILRTool_1.0.0.app/
  ```

