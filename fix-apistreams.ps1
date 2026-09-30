[Console]::OutputEncoding = [Text.Encoding]::UTF8
$b = Join-Path $PSScriptRoot 'opendoja\src\main\java'
$utf8 = New-Object System.Text.UTF8Encoding($false)

$replacements = @(
    @('com\nttdocomo\ui\_BitmapFont.java', 'return in.readAllBytes();', 'return opendoja.host.ByteStreams.readAll(in);'),
    @('com\nttdocomo\system\_SystemSupport.java', 'return stream.readAllBytes();', 'return opendoja.host.ByteStreams.readAll(stream);'),
    @('opendoja\g3d\SoftwareTexture.java', 'return inputStream.readAllBytes();', 'return opendoja.host.ByteStreams.readAll(inputStream);'),
    @('opendoja\host\DoJaRuntime.java', 'return new ByteArrayInputStream(in.readAllBytes());', 'return new ByteArrayInputStream(opendoja.host.ByteStreams.readAll(in));'),
    @('opendoja\host\ScratchpadStorage.java', 'header = input.readNBytes(HEADER_BYTES);', 'header = opendoja.host.ByteStreams.readN(input, HEADER_BYTES);'),
    @('com\acrodea\xf3\def\xfeDefaultXF2Loader.java', 'return stream == null ? null : stream.readAllBytes();', 'return stream == null ? null : opendoja.host.ByteStreams.readAll(stream);'),
    @('com\nttdocomo\ui\graphics3d\Object3D.java', 'createInstance(inputStream.readAllBytes())', 'createInstance(opendoja.host.ByteStreams.readAll(inputStream))'),
    @('opendoja\g3d\MascotLoader.java', 'loadFigure(inputStream.readAllBytes())', 'loadFigure(opendoja.host.ByteStreams.readAll(inputStream))'),
    @('opendoja\g3d\MascotLoader.java', 'loadActionTable(inputStream.readAllBytes())', 'loadActionTable(opendoja.host.ByteStreams.readAll(inputStream))'),
    @('com\nttdocomo\ui\JamNamedModuleResourceBridge.java', 'new ByteArrayInputStream(in.readAllBytes())', 'new ByteArrayInputStream(opendoja.host.ByteStreams.readAll(in))'),
    @('opendoja\audio\mld\fuetrek\FueTrekControlTables.java', 'return input.readAllBytes();', 'return opendoja.host.ByteStreams.readAll(input);'),
    @('opendoja\audio\mld\fuetrek\FueTrekMixTables.java', 'return input.readAllBytes();', 'return opendoja.host.ByteStreams.readAll(input);'),
    @('opendoja\audio\mld\fuetrek\FueTrekNoteShapeTables.java', 'byte[] data = input.readAllBytes();', 'byte[] data = opendoja.host.ByteStreams.readAll(input);'),
    @('opendoja\audio\mld\fuetrek\FueTrekResourceAudioTables.java', 'return input.readAllBytes();', 'return opendoja.host.ByteStreams.readAll(input);'),
    @('opendoja\audio\mld\fuetrek\FueTrekRom.java', 'return input.readAllBytes();', 'return opendoja.host.ByteStreams.readAll(input);'),
    @('com\nttdocomo\system\_SystemSupport.java', '.toList();', '.collect(java.util.stream.Collectors.toList());'),
    @('opendoja\host\DesktopKeyInputAdapter.java', 'pressedKeys.stream().sorted().toList()', 'pressedKeys.stream().sorted().collect(java.util.stream.Collectors.toList())')
)

$applied = 0
$missing = @()
foreach ($r in $replacements) {
    $path = Join-Path $b $r[0]
    if (-not (Test-Path $path)) { $missing += ('FILE NOT FOUND: ' + $r[0]); continue }
    $text = [IO.File]::ReadAllText($path)
    if (-not $text.Contains($r[1])) { $missing += ('PATTERN MISSING in ' + $r[0] + ': ' + $r[1]); continue }
    $text = $text.Replace($r[1], $r[2])
    [IO.File]::WriteAllText($path, $text, $utf8)
    $applied++
}
Write-Output ('applied=' + $applied)
foreach ($m in $missing) { Write-Output $m }

Write-Output '=== residual scan (built code) ==='
$files = Get-ChildItem $b -Recurse -Filter *.java | Where-Object { $_.FullName -notmatch '\\probes\\|\\tools\\|\\launcher\\' }
foreach ($f in $files) {
    $text = [IO.File]::ReadAllText($f.FullName)
    if ($text -match '\.readAllBytes\(|\.readNBytes\(') {
        foreach ($m in [regex]::Matches($text, '.*\.(readAllBytes|readNBytes)\(.*')) { Write-Output ($f.Name + ': ' + $m.Value.Trim()) }
    }
    if ($text -match '\.toList\(\)') {
        foreach ($m in [regex]::Matches($text, '.*\.toList\(\).*')) { Write-Output ($f.Name + ': ' + $m.Value.Trim()) }
    }
}
