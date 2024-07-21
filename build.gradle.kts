/*
 * by 'lemmsh' at '7/26/15 4:07 PM'
 */

plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("com.google.guava:guava:32.0.0-jre")
}
