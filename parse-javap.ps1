$t0 = Get-Date
$out = Join-Path $PSScriptRoot 'javap-all.txt'
$tsv = Join-Path $PSScriptRoot 'awt-members.tsv'
$w = [IO.StreamWriter]::new($tsv)
$w.WriteLine("bucket`ttype`tmember`topcode`treferenced_by")
$cur = ''
$total = 0
$stats = @{}
$hdr = [regex]::new('(?:class|interface|enum)\s+([A-Za-z0-9_.$]+)')
$rf = [regex]::new('^\s+\d+:\s+(\w+)\s+#\d+.*//\s+(?:InterfaceMethod|Method|Field|class)\s+(java/awt/|javax/sound/|javax/swing/)(\S+)')

foreach ($line in [IO.File]::ReadLines($out)) {
    if ($line -match '^\S.*\s\{$' -and $line -notlike 'Compiled from*') {
        $mh = $hdr.Match($line)
        if ($mh.Success) { $cur = $mh.Groups[1].Value }
    }
    if ($cur -eq '') { continue }
    $m = $rf.Match($line)
    if (-not $m.Success) { continue }
    $op = $m.Groups[1].Value
    $full = $m.Groups[2].Value + $m.Groups[3].Value
    $idx = $full.LastIndexOf(':')
    if ($idx -lt 0) { continue }
    $pre = $full.Substring(0, $idx)
    $desc = $full.Substring($idx + 1)
    $dot = $pre.LastIndexOf('.')
    if ($dot -lt 0) { continue }
    $ty = $pre.Substring(0, $dot)
    $mem = $pre.Substring($dot + 1) + ':' + $desc
    if ($cur -like 'com.nttdocomo.*' -or $cur -like 'javax.microedition.*' -or $cur -like 'com.acrodea.*' -or $cur -like 'com.docomostar.*') {
        $bucket = 'game-api'
    } elseif ($cur -like 'opendoja.launcher.*') {
        $bucket = 'launcher'
    } elseif ($cur -like 'opendoja.probes.*') {
        $bucket = 'probes'
    } elseif ($cur -like 'opendoja.tools.*') {
        $bucket = 'tools'
    } else {
        $bucket = 'opendoja-core'
    }
    $segs = $ty -split '/'
    $ns = if ($segs.Count -ge 3) { $segs[0..2] -join '/' } else { $segs -join '/' }
    if (-not $stats.ContainsKey($bucket)) { $stats[$bucket] = @{} }
    if (-not $stats[$bucket].ContainsKey($ns)) { $stats[$bucket][$ns] = @{} }
    if (-not $stats[$bucket][$ns].ContainsKey($ty)) { $stats[$bucket][$ns][$ty] = @{} }
    if (-not $stats[$bucket][$ns][$ty].ContainsKey($mem)) {
        $stats[$bucket][$ns][$ty][$mem] = 1
        $w.WriteLine("$bucket`t$ty`t$mem`t$op`t$cur")
        $total++
    }
}
$w.Close()
"unique refs written=$total elapsed=" + [int]((Get-Date) - $t0).TotalSeconds + 's'
foreach ($b in @('game-api', 'opendoja-core', 'launcher', 'probes')) {
    if (-not $stats.ContainsKey($b)) { continue }
    "=== $b :"
    foreach ($ns in ($stats[$b].Keys | Sort-Object)) {
        $types = $stats[$b][$ns].Keys
        $members = 0
        foreach ($t in $types) { $members += $stats[$b][$ns][$t].Count }
        '  {0,-18} types={1,3} members={2,5}' -f $ns, $types.Count, $members
    }
}
