@echo off
cd /d "D:\2025-studio-4"
echo Current directory: %CD%
echo.
echo Current branch:
git branch --show-current
echo.
echo Git status:
git status --porcelain
echo.
echo Adding all changes:
git add .
echo.
echo Committing changes:
git commit -m "Add ranking feature to pause menu with improved UI styling"
echo.
echo Pushing to GitHub:
git push origin main
echo.
echo Deleting feature branch:
git branch -d my-feature
echo.
echo Git operations completed.
pause
