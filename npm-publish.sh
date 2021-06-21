./gradlew build|| exit
./gradlew compileProductionExecutableKotlinJs || exit

outputDir=build/compileSync/main/productionExecutable/kotlin
cp build/tmp/jsPublicPackageJson/package.json $outputDir/
cd $outputDir || exit
npm publish