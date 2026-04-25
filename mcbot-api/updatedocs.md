cd C:\Users\lione\IdeaProjects\monkey\MinecraftBOT

./gradlew publishApiDocs

cd docs
git status
git add .
git commit -m "Update API Javadocs"
git push

cd ..
git add docs
git commit -m "Update docs submodule pointer"
git push