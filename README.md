deploy to nexus


- deploy to snapshot

```
mvn deploy:deploy-file -Durl=http://maven.qima-inc.com/content/repositories/snapshots \
                       -DrepositoryId=snapshots \
                       -Dfile=pom.xml \
                       -DpomFile=pom.xml \
                       -DgroupId=com.youzan \
                       -DartifactId=id-generator \
                       -Dversion=1.0.4-SNAPSHOT \
                       -Dpackaging=pom
                       
cd id-generator-component
mvn clean deploy                       
```

- deploy to releases
```
mvn deploy:deploy-file -Durl=http://maven.qima-inc.com/content/repositories/releases \
                       -DrepositoryId=releases \
                       -Dfile=pom.xml \
                       -DpomFile=pom.xml \
                       -DgroupId=com.youzan \
                       -DartifactId=id-generator \
                       -Dversion=1.0.4-RELEASE \
                       -Dpackaging=pom

cd id-generator-component
mvn clean deploy

```

