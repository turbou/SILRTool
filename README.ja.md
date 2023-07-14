# SILRTool
[English](https://github.com/turbou/SILRTool/blob/main/README.md) | Japanese

サーバレスのIAST解析を行う際に対象の関数にレイヤーと環境変数を追加する必要があります。  
このツールはその作業を支援します。  

## 動作環境
#### OS
- Windows8.1、 10, 11
- macOS 11(Big Sur)かそれ以上

#### Runtime
- jre1.8

## インストール
[Release](https://github.com/turbou/SILRTool/releases/latest) で幾つかのバイナリを提供しています。ビルド不要でダウンロード後すぐに使用できます。  
#### Windows
- SILRTool_X.X.X.zip  
  初回ダウンロードの場合はこちらをダウンロードして解凍して、お使いください。  
  jreフォルダ（1.8.0_202）が同梱されているため、exeの起動ですぐにツールを使用できます。
- SILRTool_X.X.X.exe  
  既にzipをダウンロード済みの場合はexeのダウンロードと入れ替えのみでツールを使用できます。
#### macOS
- SILRTool_X.X.X.cli7z  
  下記コマンドで解凍してください。  
  ```bash
  # p7zipのインストールについては
  brew install p7zip
  # 解凍コマンド
  7z x SILRTool_X.X.X.cli7z
  ```

## 事前準備（PCに対して）
`aws configure`または**手動**にてAWSのプロファイル（以下のファイル）を作成しておいてください。    
- ~/.aws/config  
  フォーマット、リージョンを設定
- ~/.aws/credentials  
  トークン、シークレットキーなどの認証情報

*AWS Cliのインストールは必須ではありません。上記のプロファイルがあれば動きます。*

## 設定について
- プロファイルについては何も指定していない場合は`default`プロファイルが採用されます。  
  それ以外のプロファイルを使用する場合はプロファイルを指定してください。  
- 設定画面で、リージョン、各レイヤーのARN、環境変数の値を設定してください。  
  これらの情報はTeamServerのサーバレスの設定タブで確認することができます。
- プロキシを経由する場合、SSL検証回避をする場合は接続設定画面で設定を行ってください。

## 使用方法
- 関数の読み込みを実行します。  
  Lambda関数の一覧が表示されます。  
  関数をダブルクリックまたはコンテキストメニューから関数の詳細を確認できます。
- レイヤーを追加、削除する関数にチェックをいれて、レイヤー登録、レイヤー削除を実行してください。

### ビルド
### gradleによるビルド
環境にあわせて`build.gradle`の以下箇所を弄ってください。
#### Windows 64bitの場合（java 64bitでEclipseなど動かしている場合はこのままで良いです）
```gradle
compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```
#### Windows 32bitの場合（exeを作るために32bit版のビルドをする場合）
```gradle
//compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
//compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```
#### macOSの場合
```gradle
//compile group: 'org.eclipse.swt', name:   'org.eclipse.swt.win32.win32.x86_64', version: '4.3'
//compile group: 'org.eclipse.swt', name: 'org.eclipse.swt.win32.win32.x86', version: '4.3'
compile group: 'org.eclipse.platform', name: 'org.eclipse.swt.cocoa.macosx.x86_64', version: '3.109.0', transitive: false
```

### コマンドプロンプト、ターミナルでビルドする場合
#### Windows
```powershell
gradlew clean jar
```
#### macOS
```bash
./gradlew clean jar
```
`build\libs`の下にjarが作成されます。

### Eclipseでビルド、実行できるようにする場合
#### Windows
```powershell
gradlew cleanEclipse eclipse
```
#### macOS
```bash
./gradlew cleanEclipse eclipse
```
Eclipseでプロジェクトをリフレッシュすると、あとはJavaの実行で`com.contrastsecurity.csvdltool.Main`クラス指定で、ツールが起動します。

## 配布
#### Windows配布用のexe化について
- launch4jを使っています。
- launch4j.xmlを読み込むと、ある程度設定が入っていて、あとはjar（ビルドによって作成された）やexeのパスを修正するぐらいです。
- jreがインストールされていない環境でも、jreフォルダを同梱することで環境に依存せずjavaを実行できるような設定になっています。  
  jreをDLして解凍したフォルダを **jre** というフォルダ名として置いておくと、優先して使用するような設定に既になっています。
- 32bit版Javaにしている理由ですが、今はもうないかもしれないですが、32bit版のwindowsの場合も想定してという感じです。
#### Mac配布用のapp化について
- javapackagerを使っています。
- jreを同梱させるため、実施するMacに1.8.0_202のJREフォルダを任意の場所に配置しておいてください。
- jarpackage.sh内の3〜7行目を適宜、修正してください。
- jarpackage.shを実行します。
  ```bash
  ./jarpackage.sh
  ```
  build/libs/bundle下にappフォルダが作られます。

#### exe, appへの署名について
まず、証明書ファイル(pfx)と証明書パスワードを入手してください。  
署名についは以下の手順で実行してください。  
#### Windows  
- エイリアスの確認
  ```powershell
  keytool -list -v -storetype pkcs12 -keystore C:\Users\turbou\Desktop\SILRTool_work\XXXXX.pfx
  # 証明書パスワードを入力
  ```
- 署名  
  launch4jのsign4jを使用します。
  ```powershell
  cd C:\Program Files (x86)\launch4j\sign4j
  sign4j.exe java -jar jsign-2.0.jar --alias 1 --keystore C:\Users\turbou\Desktop\SILRTool_work\XXXXX.pfx --storepass [パスワード] C:\Users\turbou\Desktop\SILRTool_work\common\SILRTool_1.0.0.exe
  ```
- 署名の確認  
  署名の確認については、exeを右クリック->プロパティ で確認できます。
#### macOS
- 証明書ファイルの読み込み  
  pfxファイルをダブルクリックでキーチェーンアクセス.appに読み込ませます。証明書パスワード入力が必要  
  読み込めたら、Common Name(通称)をコピー
- 署名
  ```bash
  codesign --deep -s "Contrast Security, Inc." -v SILRTool_1.0.0.app
  ```
- 署名の確認
  ```bash
  codesign -d --verbose=4 SILRTool_1.0.0.app
  ```
    
#### 圧縮について補足
- macOS
  ```bash
  7z a SILRTool_1.0.0.cli7z SILRTool_1.0.0.app/
  ```
