$rootDirectory = Get-Location

cd "h2database/gui/bin"
# Run the H2 Gui file
Start-Process -FilePath "h2-2.3.232.jar"

cd $rootDirectory
