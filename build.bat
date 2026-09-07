@echo off
setlocal enabledelayedexpansion

:: InfHell 2 - Build Script
:: Compiles and exports minecraft.jar (client) and minecraft_server.jar

set "ROOT=D:\Cosas\InfHell\infhell-release"
set "JAVAC=C:\Program Files\Java\jdk1.8.0_361\bin\javac.exe"
set "JAR=C:\Program Files\Java\jdk1.8.0_361\bin\jar.exe"

echo ==========================================
echo [1/4] Compiling client
echo ==========================================
if exist "%ROOT%\bin\client" rmdir /s /q "%ROOT%\bin\client"
mkdir "%ROOT%\bin\client"

"%JAVAC%" -d "%ROOT%\bin\client" -cp "%ROOT%\lib\client\jinput.jar;%ROOT%\lib\client\lwjgl.jar;%ROOT%\lib\client\lwjgl_util.jar;%ROOT%\lib\client\minecraft.jar" -sourcepath "%ROOT%\src\minecraft" -Xlint:none -nowarn "%ROOT%\src\minecraft\net\minecraft\client\Minecraft.java"
if errorlevel 1 (
    echo CLIENT COMPILE FAILED
    pause
    exit /b 1
)
echo Client compiled OK.

echo ==========================================
echo [2/4] Compiling server
echo ==========================================
if exist "%ROOT%\bin\server" rmdir /s /q "%ROOT%\bin\server"
mkdir "%ROOT%\bin\server"

"%JAVAC%" -d "%ROOT%\bin\server" -cp "%ROOT%\lib\server\minecraft_server.jar" -sourcepath "%ROOT%\src\minecraft_server" -Xlint:none -nowarn "%ROOT%\src\minecraft_server\net\minecraft\server\MinecraftServer.java"
if errorlevel 1 (
    echo SERVER COMPILE FAILED
    pause
    exit /b 1
)
echo Server compiled OK.

echo ==========================================
echo [3/4] Creating minecraft.jar
echo ==========================================
if exist "%ROOT%\minecraft.jar" del "%ROOT%\minecraft.jar"
if exist "%ROOT%\tmpjar" rmdir /s /q "%ROOT%\tmpjar"
mkdir "%ROOT%\tmpjar"

mkdir "%ROOT%\tmpjar\META-INF"
(
echo Manifest-Version: 1.0
echo Created-By: InfHell 2
echo Main-Class: net.minecraft.client.Minecraft
) > "%ROOT%\tmpjar\META-INF\MANIFEST.MF"

mkdir "%ROOT%\tmpjar\audio"
pushd "%ROOT%\tmpjar\audio"
"%JAR%" xf "%ROOT%\lib\client\minecraft.jar" paulscode com
popd

mkdir "%ROOT%\tmpjar\resources"
pushd "%ROOT%\tmpjar\resources"
"%JAR%" xf "%ROOT%\lib\client\minecraft.jar" font.txt pack.png pack.txt particles.png terrain.png
"%JAR%" xf "%ROOT%\lib\client\minecraft.jar" achievement armor art environment font gui item misc mob terrain lang pe
popd

:: Add custom resources from src/minecraft/resources
"%JAR%" cfm "%ROOT%\minecraft.jar" "%ROOT%\tmpjar\META-INF\MANIFEST.MF" -C "%ROOT%\bin\client" .
"%JAR%" uf "%ROOT%\minecraft.jar" -C "%ROOT%\tmpjar\audio" .
"%JAR%" uf "%ROOT%\minecraft.jar" -C "%ROOT%\tmpjar\resources" .
"%JAR%" uf "%ROOT%\minecraft.jar" -C "%ROOT%\src\minecraft\lang" .
"%JAR%" uf "%ROOT%\minecraft.jar" -C "%ROOT%\src\minecraft\resources" .
pushd "%ROOT%\src\minecraft"
"%JAR%" uf "%ROOT%\minecraft.jar" title\black.png title\mojang.png title\splashes.txt
"%JAR%" uf "%ROOT%\minecraft.jar" seasons\fogColor0.png seasons\frozen.png seasons\leavesColor0.png seasons\skyColor0.png
popd

echo minecraft.jar created.

echo ==========================================
echo [4/4] Creating minecraft_server.jar
echo ==========================================
if exist "%ROOT%\minecraft_server.jar" del "%ROOT%\minecraft_server.jar"

if exist "%ROOT%\tmpjar\META-INF" rmdir /s /q "%ROOT%\tmpjar\META-INF"
mkdir "%ROOT%\tmpjar\META-INF"
(
echo Manifest-Version: 1.0
echo Created-By: InfHell 2
echo Main-Class: net.minecraft.server.MinecraftServer
) > "%ROOT%\tmpjar\META-INF\MANIFEST.MF"

:: Add server resources from src/minecraft_server/resources
"%JAR%" cfm "%ROOT%\minecraft_server.jar" "%ROOT%\tmpjar\META-INF\MANIFEST.MF" -C "%ROOT%\bin\server" .
"%JAR%" uf "%ROOT%\minecraft_server.jar" -C "%ROOT%\src\minecraft_server\resources" .

echo minecraft_server.jar created.

rmdir /s /q "%ROOT%\tmpjar" 2>nul

echo.
echo ==========================================
echo BUILD COMPLETE!
echo.
echo Client:  %ROOT%\minecraft.jar
echo Server:  %ROOT%\minecraft_server.jar
echo ==========================================
pause
