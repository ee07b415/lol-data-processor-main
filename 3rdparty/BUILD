SCALA_REV = 2.12

jar_library(name='protobuf-3.10.0',
    jars = [
        jar(org='com.google.protobuf', name='protobuf-java', rev='3.10.0'),
    ],
    # javac requires protobuf to be present in order to compile the MessageBuilders API.
    scope='forced',)

target(name='protobuf-java', dependencies = [':protobuf-3.10.0'])
