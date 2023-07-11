# SILRTool
*Serverless Instrumentation Layers Registration Tool*  

### 現在まだプロトです。

### 前提条件
```aws configure```または手動にてAWSのプロファイル（以下のファイル）を作成しておいてください。    
- ~/.aws/config
- ~/.aws/credentials

**AWS Cliのインストールは任意となります。**

### Eclipseでの起動の仕方
今のbuild.gradleはMacでそのまま動くようになっています。  
java -versionでjdk1.8であることを確認してください。  
```bash
./gradlew cleanEclipse eclipse
```
WindowsのEclipseで動かす場合はbuild.gradleの以下の部分を弄ってください。  
```gradle
compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```

あとはEclipseでプロジェクトをリフレッシュして実行してください。  
メインクラスは```com.contrastsecurity.silrtool.Main```を選んでください。

#### macOS (jar to app)
1. Using javapackager.
1. If bundle jre, place the jre folder anywhere on the file system.
1. If needed, correct lines 3 to 7 in jarpackage.sh.
1. run `jarpackage.sh`.  
    ```bash
    ./jarpackage.sh
    ```
    app folder will be created under `build\libs/bundle`.

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

以上
