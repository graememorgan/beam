/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * License); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.beam.gradle

import org.gradle.api.Project

/**
 * Utilities for working with our vendored version of gRPC.
 */
class GrpcVendoring_1_26_0 {
  /** Returns the list of compile time dependencies. */
  static List<String> dependencies() {
    return [
      'com.google.guava:guava:26.0-jre',
      'com.google.protobuf:protobuf-java:3.11.0',
      'com.google.protobuf:protobuf-java-util:3.11.0',
      'com.google.code.gson:gson:2.8.6',
      'io.grpc:grpc-auth:1.26.0',
      'io.grpc:grpc-core:1.26.0',
      'io.grpc:grpc-context:1.26.0',
      'io.grpc:grpc-netty:1.26.0',
      'io.grpc:grpc-protobuf:1.26.0',
      'io.grpc:grpc-stub:1.26.0',
      'io.netty:netty-transport-native-epoll:4.1.42.Final',
      // tcnative version from https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty
      'io.netty:netty-tcnative-boringssl-static:2.0.26.Final',
      'com.google.auth:google-auth-library-credentials:0.18.0',
      'io.grpc:grpc-testing:1.26.0',
      'com.google.api.grpc:proto-google-common-protos:1.12.0',
      'io.opencensus:opencensus-api:0.24.0',
      'io.opencensus:opencensus-contrib-grpc-metrics:0.24.0',
      'io.perfmark:perfmark-api:0.19.0',
      'com.github.jponge:lzma-java:1.3',
      'com.google.protobuf.nano:protobuf-javanano:3.0.0-alpha-5',
      'com.jcraft:jzlib:1.1.3',
      'com.ning:compress-lzf:1.0.3',
      'net.jpountz.lz4:lz4:1.3.0',
      'org.bouncycastle:bcpkix-jdk15on:1.54',
      'org.bouncycastle:bcprov-jdk15on:1.54',
      'org.conscrypt:conscrypt-openjdk-uber:1.3.0',
      'org.eclipse.jetty.alpn:alpn-api:1.1.2.v20150522',
      'org.eclipse.jetty.npn:npn-api:1.1.1.v20141010',
      'org.jboss.marshalling:jboss-marshalling:1.4.11.Final',
      'org.jboss.modules:jboss-modules:1.1.0.Beta1'
    ]
  }

  /**
   * Returns the list of runtime time dependencies that should be exported as runtime
   * dependencies within the vendored jar.
   */
  static List<String> runtimeDependencies() {
    return [
      'com.google.errorprone:error_prone_annotations:2.3.3',
      'commons-logging:commons-logging:1.2',
      'org.apache.logging.log4j:log4j-api:2.6.2',
      'org.slf4j:slf4j-api:1.7.21'
    ]
  }

  /**
   * Returns the list of test dependencies.
   */
  static List<String> testDependencies() {
    return [
      'junit:junit:4.12',
    ]
  }

  static Map<String, String> relocations() {
    // The relocation paths below specifically use gRPC and the full version string as
    // the code relocation prefix. See https://lists.apache.org/thread.html/4c12db35b40a6d56e170cd6fc8bb0ac4c43a99aa3cb7dbae54176815@%3Cdev.beam.apache.org%3E
    // for further details.

    // To produce the list of necessary relocations, one needs to start with a set of target
    // packages that one wants to vendor, find all necessary transitive dependencies of that
    // set and provide relocations for each such that all necessary packages and their
    // dependencies are relocated. Any optional dependency that doesn't need relocation
    // must be excluded via an 'exclude' rule. There is additional complexity of libraries that use
    // JNI or reflection and have to be handled on case by case basis by learning whether
    // they support relocation and how would one go about doing it by reading any documentation
    // those libraries may provide. The 'validateShadedJarDoesntLeakNonOrgApacheBeamClasses'
    // ensures that there are no classes outside of the 'org.apache.beam' namespace.

    String version = "v1p26p0";
    String prefix = "org.apache.beam.vendor.grpc.${version}";
    List<String> packagesToRelocate = [
      // guava uses the com.google.common and com.google.thirdparty package namespaces
      "com.google.common",
      "com.google.thirdparty",
      "com.google.protobuf",
      "com.google.gson",
      "com.google.auth",
      "com.google.api",
      "com.google.cloud",
      "com.google.logging",
      "com.google.longrunning",
      "com.google.rpc",
      "com.google.type",
      "io.grpc",
      "io.netty",
      "io.opencensus",
      "io.perfmark",
      "com.google.protobuf.nano",
      "com.jcraft",
      "com.ning",
      "com.sun",
      "lzma",
      "net.jpountz",
      "org.bouncycastle",
      "org.cservenak.streams",
      "org.conscrypt",
      "org.eclipse.jetty.alpn",
      "org.eclipse.jetty.npn",
      "org.jboss.marshalling",
      "org.jboss.modules"
    ]

    return packagesToRelocate.collectEntries {
      [ (it): "${prefix}.${it}" ]
    } + [
      // Adapted from https://github.com/grpc/grpc-java/blob/e283f70ad91f99c7fee8b31b605ef12a4f9b1690/netty/shaded/build.gradle#L41
      // We       "io.netty": "${prefix}.io.netty",have to be careful with these replacements as they must not match any
      // string in NativeLibraryLoader, else they cause corruption. Note that
      // this includes concatenation of string literals and constants.
      'META-INF/native/libnetty': "META-INF/native/liborg_apache_beam_vendor_grpc_${version}_netty",
      'META-INF/native/netty': "META-INF/native/org_apache_beam_vendor_grpc_${version}_netty",
    ]
  }

  /** Returns the list of shading exclusions. */
  static List<String> exclusions() {
    return [
      // Don't include android annotations, errorprone, checkerframework, JDK8 annotations, objenesis, junit,
      // commons-logging, log4j, slf4j and mockito in the vendored jar
      "android/annotation/**/",
      "com/google/errorprone/**",
      "com/google/instrumentation/**",
      "com/google/j2objc/annotations/**",
      "javax/annotation/**",
      "junit/**",
      "org/apache/commons/logging/**",
      "org/apache/log/**",
      "org/apache/log4j/**",
      "org/apache/logging/log4j/**",
      "org/checkerframework/**",
      "org/codehaus/mojo/animal_sniffer/**",
      "org/hamcrest/**",
      "org/junit/**",
      "org/mockito/**",
      "org/objenesis/**",
      "org/slf4j/**",
    ]
  }

  /**
   * Returns a closure contaning the dependencies map used for shading gRPC within the main
   * Apache Beam project.
   */
  static Object dependenciesClosure() {
    return {
      dependencies().each { compile it }
      runtimeDependencies().each { shadow it }
    }
  }

  /**
   * Returns a closure with the code relocation configuration for shading gRPC within the main
   * Apache Beam project.
   */
  static Object shadowClosure() {
    return {
      relocations().each { srcNamespace, destNamespace ->
        relocate srcNamespace, destNamespace
      }
      exclusions().each { exclude it }
    }
  }
}
