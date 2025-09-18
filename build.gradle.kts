plugins {
    java
    application
}
application { 
    mainClass.set("ImageAnalysisQuickstart")
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(group = "com.microsoft.azure.cognitiveservices", name = "azure-cognitiveservices-computervision", version = "1.0.9-beta")
}