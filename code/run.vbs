Set WshShell = CreateObject("WScript.Shell")

' First compile the Java files
WshShell.Run "javac -cp "".;C:\Users\Shinya\Downloads\stuff\mysql-connector-j-8.0.33\mysql-connector-j-8.0.33\mysql-connector-j-8.0.33.jar"" *.java", 1, True

' Then run the main class
WshShell.Run "java -cp "".;C:\Users\Shinya\Downloads\stuff\mysql-connector-j-8.0.33\mysql-connector-j-8.0.33\mysql-connector-j-8.0.33.jar"" Main", 0, True