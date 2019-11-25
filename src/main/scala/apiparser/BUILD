scala_library(
    name='apiparser-lib',
    sources = globs("*.scala"),
    dependencies=[
        "src/main/scala/apiparser/job:job",
        "src/main/scala/apiparser/job/dataflow:dataflow",
        "src/main/scala/apiparser/model/transformed:transformed",
        "3rdparty/jvm:externalLib"
    ]
)

jvm_binary(
    name='apiparser',
    source = 'Main.scala',
    main = 'apiparser.Main',
    dependencies = [
        ':apiparser-lib'
    ]
)

jvm_app(
    name = 'apibundle',
    dependencies = [
        ':apiparser-lib'
    ],
    bundles = []
)
