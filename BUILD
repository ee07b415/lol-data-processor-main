files(
  name = '3rdparty_directory',
  sources = rglobs('3rdparty/*'),
)

files(
  name = 'pants_ini',
  source = 'pants.ini',
)

target(
  name = 'default_target',
  dependencies = [
    'src/main/scala/apiparser:apiparser',
  ]
)
