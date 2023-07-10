# ServerlessTool
現在まだプロトです。

### 前提条件
aws configureで、AWS Cliを使うための設定を済ませておいてください。

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
メインクラスは```mori.Main```を選んでください。

あとで、exeで実行できるバイナリなども用意していきます。

以上
