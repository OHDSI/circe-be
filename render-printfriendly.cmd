@echo off
setlocal

if "%~1"=="" (
  echo Usage: %~nx0 ^<cohort-expression.json^>
  exit /b 2
)

if not exist "%~f1" (
  echo Input JSON file was not found: %~f1
  exit /b 2
)

pushd "%~dp0"
call mvnw.cmd "-Dprintfriendly.input=%~f1" -Dtest=PrintFriendlyTest#previewLocalExpression test
set "EXIT_CODE=%ERRORLEVEL%"
popd

exit /b %EXIT_CODE%