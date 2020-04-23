Set oShell = CreateObject ("Wscript.Shell") 
Dim strArgs
strArgs = "cmd /c RadioTT.bat"
oShell.Run strArgs, 0, false