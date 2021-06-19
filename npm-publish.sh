./gradlew compileKotlinJs
./gradlew compileProductionExecutableKotlinJs
./gradlew jsPublicPackageJson

outputDir=build/compileSync/main/productionExecutable/kotlin
cp build/tmp/jsPublicPackageJson/package.json $outputDir/
cd outputDir || exit
npm publish