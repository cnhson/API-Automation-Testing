$rootDirectory = Get-Location

mvn clean test

Start-Sleep -Seconds 1

cd "target/surefire-reports/"

# Step 5: Get the dynamic folder name (assumes only one folder exists, adjust as needed)
$folderName = Get-ChildItem -Directory | Select-Object -ExpandProperty Name

cd $folderName

$htmlFileName = Get-ChildItem -Filter *.html | Select-Object -ExpandProperty Name
Invoke-Item $htmlFileName

cd $rootDirectory
