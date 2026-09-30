$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$srcJava = Join-Path $root 'opendoja\src\main\java'
$outRoot = Join-Path $srcJava 'opendoja\compat'
if (Test-Path $outRoot) { Remove-Item $outRoot -Recurse -Force }

# ---------------- helpers ----------------
function Map-Dotted([string]$d) {
    $d = $d -replace 'java\.awt\.', 'opendoja.compat.awt.'
    $d = $d -replace 'javax\.sound\.', 'opendoja.compat.sound.'
    $d = $d -replace 'javax\.swing\.', 'opendoja.compat.swing.'
    return $d
}
function Map-Slash([string]$s) {
    $s = $s -replace '^java/awt/', 'opendoja/compat/awt/'
    $s = $s -replace '^javax/sound/', 'opendoja/compat/sound/'
    $s = $s -replace '^javax/swing/', 'opendoja/compat/swing/'
    return $s
}
function Unmap-Dotted([string]$d) {
    $d = $d -replace '^opendoja\.compat\.awt\.', 'java.awt.'
    $d = $d -replace '^opendoja\.compat\.sound\.', 'javax.sound.'
    $d = $d -replace '^opendoja\.compat\.swing\.', 'javax.swing.'
    return $d
}
function Convert-FieldDesc([string]$d) {
    $arr = 0
    while ($d.StartsWith('[')) { $arr++; $d = $d.Substring(1) }
    $c = $d.Substring(0, 1)
    $base = switch ($c) {
        'B' { 'byte' } 'C' { 'char' } 'D' { 'double' } 'F' { 'float' }
        'I' { 'int' } 'J' { 'long' } 'S' { 'short' } 'Z' { 'boolean' } 'V' { 'void' }
        'L' {
            $inner = $d.Substring(1, $d.Length - 2) -replace '/', '.'
            $inner = Map-Dotted $inner
            $inner -replace '\$', '.'
        }
        default { throw "bad field desc: $d" }
    }
    return $base + ('[]' * $arr)
}
function Convert-MethodDesc([string]$d) {
    $pi = $d.IndexOf('('); $ci = $d.IndexOf(')')
    $ps = $d.Substring($pi + 1, $ci - $pi - 1)
    $params = @()
    foreach ($t in [regex]::Matches($ps, '\[*(?:[BCDFIJSZ]|L[^;]+;)')) {
        $params += Convert-FieldDesc $t.Value
    }
    $ret = Convert-FieldDesc $d.Substring($ci + 1)
    return @{ params = $params; ret = $ret }
}
function Get-SimpleName([string]$p) {
    $p = ($p -replace '\s', '')
    while ($p -match '<') { $n = $p; $p = $p -replace '<[^<>]*>', ''; if ($p -eq $n) { break } }
    $arr = ''
    while ($p.EndsWith('[]')) { $arr += '[]'; $p = $p.Substring(0, $p.Length - 2) }
    $last = ($p -split '\.')[-1]
    $last = ($last -split '\$')[-1]
    return $last + $arr
}
function Join-Params($params) {
    $out = @()
    $i = 0
    foreach ($p in $params) { $out += "$p p$i"; $i++ }
    return ($out -join ', ')
}
function Split-TopCommas([string]$s) {
    $res = New-Object System.Collections.ArrayList
    $depth = 0; $cur = ''
    foreach ($ch in $s.ToCharArray()) {
        if ($ch -eq '<') { $depth++ }
        elseif ($ch -eq '>') { $depth-- }
        if ($ch -eq ',' -and $depth -eq 0) { [void]$res.Add($cur); $cur = '' }
        else { $cur += $ch }
    }
    if ($cur.Trim()) { [void]$res.Add($cur) }
    return $res
}
    function Default-Expr([string]$t) {
        if ($t -match '\[\]') { return 'null' }
        $b = $t -replace '(\[\])+$', ''
    if ($b -match '^(int|short|byte|char)$') { return '0' }
    if ($b -eq 'long') { return '0L' }
    if ($b -eq 'float') { return '0f' }
    if ($b -eq 'double') { return '0d' }
    if ($b -eq 'boolean') { return 'false' }
    return 'null'
}

# ---------------- read TSV ----------------
$tsv = Import-Csv (Join-Path $root 'awt-members.tsv') -Delimiter "`t"
$shimRows = @($tsv | Where-Object {
    $_.bucket -in @('game-api', 'opendoja-core') -and
    ($_.type -like 'java/awt/*' -or $_.type -like 'javax/sound/*' -or $_.type -like 'javax/swing/*')
})

# ---------------- parse JDK sigs ----------------
$classHeaders = @{}   # 'java.awt.AlphaComposite' or 'java.awt.geom.Path2D$Float' -> header line
$throwsMap = @{}      # 'Owner|name/p1,p2' -> throws clause string
$throwsGlobal = @{}   # 'name/p1,p2' -> throws clause string
$currentClass = $null
foreach ($line in Get-Content (Join-Path $root 'jdk-sigs-all.txt')) {
    if ($line -match '^\S.*\s\{$' -and $line -notlike 'Compiled from*') {
        if ($line -match '(?:class|interface|enum)\s+([A-Za-z0-9_.$]+)') {
            $currentClass = $Matches[1]
            $classHeaders[$currentClass] = $line
        }
        continue
    }
    if (-not $currentClass) { continue }
    $t = $line.Trim()
    if ($t -notmatch ';$') { continue }
    if ($t -match '^\w*\s*static\s*\{') { continue }
    $pi = $t.IndexOf('(')
    if ($pi -lt 0) { continue }
    if ($t -notmatch '([\w.$]+)\(') { continue }
    $nm = $Matches[1]
    $nmSimple = ($nm -split '\.')[-1]
    $throws = ''
    if ($t -match '\)\s*throws\s+([^;]+);') { $throws = $Matches[1] }
    $pe = $t.IndexOf(')')
    $paramsRaw = $t.Substring($pi + 1, $pe - $pi - 1)
    $simp = @()
    if ($paramsRaw.Trim()) {
        $tmp = $paramsRaw
        while ($tmp -match '<') { $n2 = $tmp; $tmp = $tmp -replace '<[^<>]*>', ''; if ($tmp -eq $n2) { break } }
        foreach ($p in ($tmp -split ',')) { $simp += Get-SimpleName $p }
    }
    $key = $nmSimple + '/' + ($simp -join ',')
    $throwsMap[($currentClass + '|' + $key)] = $throws
    if (-not $throwsGlobal.ContainsKey($key)) { $throwsGlobal[$key] = $throws
        }
}

# ---------------- build node tree ----------------
function New-Node { return @{ own = New-Object System.Collections.ArrayList; kids = @{}; cfields = New-Object System.Collections.ArrayList } }
$tree = @{}
function Add-Row([string]$type, $row) {
    $i = $type.IndexOf('$')
    $top = if ($i -lt 0) { $type } else { $type.Substring(0, $i) }
    if (-not $tree.ContainsKey($top)) { $tree[$top] = New-Node }
    $node = $tree[$top]
    if ($i -ge 0) {
        foreach ($seg in ($type.Substring($i + 1).Split('$'))) {
            if (-not $node.kids.ContainsKey($seg)) { $node.kids[$seg] = New-Node }
            $node = $node.kids[$seg]
        }
    }
    [void]$node.own.Add($row)
}
foreach ($r in $shimRows) { Add-Row $r.type $r }

# ---------------- headers for auto-expand ----------------
$pending = New-Object System.Collections.Generic.Queue[string]
$expandedTops = @{}   # dotted compat top fqn -> $true

function Get-TopDotted([string]$dotted) {
    $i = $dotted.IndexOf('$')
    if ($i -ge 0) { return $dotted.Substring(0, $i) }
    $j = $dotted.LastIndexOf('.')
    # may be nested unknown: heuristic handled by caller
    return $dotted
}
foreach ($k in $tree.Keys) {
    $expandedTops[(Map-Dotted ($k -replace '/', '.'))] = $true
}

# collect referenced compat types from rows (deferred to emission time we queue via headers too)
function Queue-If-Compat([string]$t) {
    $t = $t -replace '\[\]', ''
    if ($t -like '*RenderingHints*') { Write-Output ('[q] ' + $t) }
    if ($t -like 'opendoja.compat.awt*' -or $t -like 'opendoja.compat.sound*' -or $t -like 'opendoja.compat.swing*') {
        $pending.Enqueue($t)
    }
}

# ---------------- emission ----------------
$written = New-Object System.Collections.Generic.List[string]

function Get-EnumSkip([string]$nm) {
    return $nm -in @('ordinal', 'name', 'clone', 'getClass', 'wait', 'notify', 'notifyAll',
        'compareTo', 'equals', 'hashCode', 'describeConstable', 'getDeclaringClass',
        'valueOf', 'values')
}

$specialIface = @{
    'opendoja.compat.sound.midi.MetaEventListener' = @('void meta(opendoja.compat.sound.midi.MetaMessage p0)')
    'opendoja.compat.awt.event.ActionListener' = @('void actionPerformed(opendoja.compat.awt.event.ActionEvent p0)')
}

function Emit-File([string]$topKey) {
    $node = $tree[$topKey]
    $topSlash = Map-Slash $topKey
    $segs = $topSlash.Split('/')
    $pkg = ($segs[0..($segs.Count - 2)]) -join '.'
    $topSimple = $segs[-1]
    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("package $pkg;")
    $lines.Add("")
    Emit-Node $node $topSlash $topSimple $false 0 $lines
    $path = Join-Path $srcJava (($topSlash -replace '/', '\') + '.java')
    $dir = Split-Path $path -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
    [IO.File]::WriteAllText($path, (($lines -join "`r`n") + "`r`n"), [Text.UTF8Encoding]::new($false))
    [void]$written.Add($path)
    Write-Output ('[f] ' + $topKey)
}

function Emit-Node([hashtable]$node, [string]$slashBinary, [string]$simple, [bool]$isNested, [int]$indent, $lines) {
    $ind = ' ' * $indent
    $origDotted = $slashBinary -replace '/', '.'
    $sigKey = Unmap-Dotted $origDotted
    $header = $null
    if ($classHeaders.ContainsKey($sigKey)) { $header = $classHeaders[$sigKey] }

    $kind = 'class'
    $rest = ''
    $isEnum = $false
    $isIface = $false
    $mods = 'public'
    if ($header) {
        if ($header -match '^(?<mods>[\w\s]*?)?(?<kind>class|interface|enum)\s+(?<name>[\w.$]+)(?<rest>.*)$') {
            $kind = $Matches['kind']
            $rest = $Matches['rest']
            $hm = $Matches['mods']
            $toks = @()
            foreach ($w in ($hm -split '\s+')) {
                if ($w -in @('public', 'protected', 'private', 'static')) { $toks += $w }
            }
            if ($toks.Count -gt 0) { $mods = ($toks -join ' ') }
            if ($toks -notcontains 'public' -and $toks -notcontains 'protected' -and $toks -notcontains 'private') { $mods = 'public' + $(if ($toks -contains 'static') { ' static' } else { '' }) }
        }
    }
    if ($header -and $header -match 'extends\s+java\.lang\.Enum<') { $kind = 'enum' }
    if ($kind -eq 'interface') { $isIface = $true }
    if ($kind -eq 'enum') { $isEnum = $true }
    $rest = $rest.TrimEnd('{').Trim()
    if ($isEnum) {
        $rest = ($rest -replace 'extends\s+java\.lang\.Enum<[^>]*>', '').Trim()
    }
    $rest = Map-Dotted $rest
    $rest = $rest.Replace('$', '.')
    if ($rest -match 'javax\.accessibility\.') {
        $rest = $rest -replace 'implements\s+javax\.accessibility\.[\w.$]+\s*,\s*', 'implements '
        $rest = $rest -replace ',\s*javax\.accessibility\.[\w.$]+(?=\s*,)', ''
        $rest = $rest -replace ',\s*javax\.accessibility\.[\w.$]+\s*$', ''
        $rest = $rest -replace 'implements\s+javax\.accessibility\.[\w.$]+\s*$', ''
        $rest = ($rest -replace '\bimplements\s*$', '').Trim()
    }
    if ($kind -eq 'class') {
        if ($isNested -and ($mods -notlike '*static*')) { $mods = "$mods static" }
    } elseif ($mods -like '*static*') {
        $mods = (($mods -split '\s+') | Where-Object { $_ -and $_ -ne 'static' }) -join ' '
    }

    $decl = "$ind$mods $($kind) $simple"
    if ($rest) { $decl = "$decl $rest" }
    $lines.Add("$decl {")
    $headerIdx = $lines.Count - 1
    if ($isEnum) { $lines.Add("    ;"); $headerIdx++ }

    $inner = $indent + 4
    $iind = ' ' * $inner
    $seen = @{}
    $seenFieldName = @{}
    $sigSeen = @{}
    $hasNoArgCtor = $false
    $parentNeedsSuper = $false
    $parentSuperExpr = ''
    $realParentFqn = ''
    if (-not $isIface -and -not $isEnum -and $rest -match '\bextends\s+([\w.$]+)') {
        $rp = $Matches[1]
        if ($rp -notlike 'opendoja.compat.*') {
            $realParentFqn = $rp
            $pi2 = Get-ParentSuper $rp
            if (-not $pi2.noArg) { $parentNeedsSuper = $true; $parentSuperExpr = $pi2.expr }
        }
    }

    foreach ($r in $node.own) {
        $mi = $r.member.LastIndexOf(':')
        if ($mi -lt 0) { continue }
        $nm = $r.member.Substring(0, $mi)
        $desc = $r.member.Substring($mi + 1)
        if ($seen.ContainsKey($nm + ':' + $desc)) { continue }
        $seen[$nm + ':' + $desc] = $true
        if ($nm.Trim('"') -eq '<clinit>') { continue }
        if ($isEnum -and -not $isIface -and (Get-EnumSkip ($nm.Trim('"')))) { continue }

        if ($desc.StartsWith('(')) {
            $md = Convert-MethodDesc $desc
            $psig = Join-Params $md.params
            if ($nm.Trim('"') -eq '<init>') {
                if ($isIface -or $isEnum) { continue }
                if ($md.params.Count -eq 0) { $hasNoArgCtor = $true }
                $cbody = '{ }'
                if ($parentNeedsSuper) { $cbody = '{ super(' + $parentSuperExpr + '); }' }
                $lines.Add("${iind}public $simple($psig) $cbody")
                continue
            }
            $isStatic = ($r.opcode -eq 'invokestatic')
            $ownerKey = ($sigKey + '|' + $nm + '/' + ((@($md.params | ForEach-Object { Get-SimpleName $_ })) -join ','))
            $spKey = ($nm + '/' + ((@($md.params | ForEach-Object { Get-SimpleName $_ })) -join ','))
            $sigSeen[$spKey] = $true
            $throws = ''
            if ($throwsMap.ContainsKey($ownerKey)) { $throws = $throwsMap[$ownerKey] }
            elseif ($throwsGlobal.ContainsKey($spKey)) { $throws = $throwsGlobal[$spKey] }
            $throwsTxt = ''
            if ($throws) {
                $mapped = @()
                foreach ($tx in ($throws -split ',')) { $mapped += (Map-Dotted $tx.Trim()) }
                $throwsTxt = ' throws ' + ($mapped -join ', ')
            }
            $ret = $md.ret
            if ($isIface) {
                if ($isStatic) {
                    $lines.Add("${iind}public static $ret $nm($psig) $throwsTxt { $(if ($ret -ne 'void') { "return $(Default-Expr $ret);" }) }")
                } else {
                    $lines.Add("${iind}default $ret $nm($psig) $throwsTxt { $(if ($ret -ne 'void') { "return $(Default-Expr $ret);" }) }")
                }
            } else {
                if ($isStatic) {
                    $lines.Add("${iind}public static $ret $nm($psig) $throwsTxt { $(if ($ret -ne 'void') { "return $(Default-Expr $ret);" }) }")
                } else {
                    $lines.Add("${iind}public $ret $nm($psig) $throwsTxt { $(if ($ret -ne 'void') { "return $(Default-Expr $ret);" }) }")
                }
            }
            Queue-If-Compat $ret
            foreach ($pp in $md.params) { Queue-If-Compat $pp }
        } else {
            $ftype = Convert-FieldDesc $desc
            $seenFieldName[$nm] = $true
            $isStatic = ($r.opcode -in @('getstatic', 'putstatic'))
            if ($isIface) {
                $lines.Add("${iind}public static final $ftype $nm = $(Default-Expr $ftype);")
            } elseif ($isStatic) {
                $lines.Add("${iind}public static $ftype $nm = $(Default-Expr $ftype);")
            } else {
                $lines.Add("${iind}public $ftype $nm = $(Default-Expr $ftype);")
            }
            Queue-If-Compat $ftype
        }
    }
    foreach ($cf in @($node.cfields)) {
        $parts = $cf -split '\|', 3
        if ($parts.Count -lt 3) { continue }
        if ($seenFieldName.ContainsKey($parts[1])) { continue }
        $seenFieldName[$parts[1]] = $true
        $lines.Add("${iind}public static final $($parts[0]) $($parts[1]) = $($parts[2]);")
    }
    if ($isIface -and $specialIface.ContainsKey(($slashBinary -replace '/', '.'))) {
        foreach ($sm in $specialIface[($slashBinary -replace '/', '.')]) {
            if ($sm -match '^(?<ret>[\w.$\[\]]+)\s+(?<nm>\w+)\((?<ps>[^)]*)\)$') {
                $smParams = @()
                foreach ($p in (Split-TopCommas $Matches['ps'])) { if ($p.Trim()) { $smParams += $p.Trim() } }
                $smKey = $Matches['nm'] + '/' + ((@($smParams | ForEach-Object { Get-SimpleName $_ })) -join ',')
                if ($sigSeen.ContainsKey($smKey)) { continue }
                $sigSeen[$smKey] = $true
                $lines.Add("${iind}public $sm;")
            }
        }
    }
    if (-not $isIface -and -not $isEnum -and $rest -match 'implements\s+(.+)$') {
        $implBare = $Matches[1]
        while ($implBare -match '<') { $n2 = $implBare; $implBare = $implBare -replace '<[^<>]*>', ''; if ($implBare -eq $n2) { break } }
        foreach ($itfX in ($implBare -split ',')) {
            $itf = $itfX.Trim()
            if (-not $itf) { continue }
            if ($itf -like 'opendoja.compat.*') { continue }
            if ($itf -in @('java.lang.Cloneable', 'java.io.Serializable', 'java.util.EventListener',
                    'java.lang.AutoCloseable', 'java.io.Closeable', 'java.lang.Iterable')) { continue }
            Write-Output ('[ri] ' + $itf); $mls = Get-RealIfaceMethods $itf; Write-Output ('[ri2] count=' + @($mls).Count); foreach ($ml in $mls) {
                if ($ml -notmatch '^(?<head>.*?)\s*(?<nm>\w+)\((?<params>[^)]*)\)(?<rest2>.*)$') { continue }
                $mm = $Matches
                $rnm = $mm['nm']
                $modSet = @()
                $words = @($mm['head'].Trim() -split '\s+' | Where-Object { $_ })
                $modWords = @('public', 'protected', 'private', 'abstract', 'final', 'static', 'default', 'strictfp', 'native', 'synchronized')
                while ($words.Count -gt 0 -and ($modWords -contains $words[0])) {
                    $modSet += $words[0]
                    $words = @($words | Select-Object -Skip 1)
                }
                if ($modSet -contains 'static' -or $modSet -contains 'default') { continue }
                if ($rnm -in @('equals', 'hashCode', 'toString', 'clone', 'getClass', 'wait',
                        'notify', 'notifyAll', 'finalize')) { continue }
                $mret = ($words -join ' ')
                if (-not $mret -or $mret.StartsWith('<')) { continue }
                $mret = ($mret -replace '\b([A-Z])\b', 'java.lang.Object') -replace '\$', '.'
                $mp = @()
                $praw = ($mm['params'] -replace '\b([A-Z])\b', 'java.lang.Object') -replace '\$', '.'
                if ($praw.Trim()) {
                    foreach ($p in (Split-TopCommas $praw)) {
                        if ($p.Trim()) { $mp += $p.Trim() }
                    }
                }
                $skey = $rnm + '/' + ((@($mp | ForEach-Object { Get-SimpleName $_ })) -join ',')
                if ($sigSeen.ContainsKey($skey)) { continue }
                $sigSeen[$skey] = $true
                $psig = Join-Params $mp
                if ($mret -eq 'void') {
                    $lines.Add("${iind}public void $rnm($psig) { }")
                } else {
                    $lines.Add("${iind}public $mret $rnm($psig) { return $(Default-Expr $mret); }")
                }
            }
        }
    }
    if ($realParentFqn) {
        foreach ($ml in (Get-RealIfaceMethods $realParentFqn)) {
            if ($ml -notmatch '^(?<head>.*?)\s*(?<nm>\w+)\((?<params>[^)]*)\)(?<rest2>.*)$') { continue }
            $mm = $Matches
            $rnm = $mm['nm']
            $modSet = @()
            $words = @($mm['head'].Trim() -split '\s+' | Where-Object { $_ })
            $modWords = @('public', 'protected', 'private', 'abstract', 'final', 'static', 'default', 'strictfp', 'native', 'synchronized')
            while ($words.Count -gt 0 -and ($modWords -contains $words[0])) {
                $modSet += $words[0]
                $words = @($words | Select-Object -Skip 1)
            }
            if ($modSet -notcontains 'abstract') { continue }
            if ($rnm -eq ($realParentFqn -split '\.')[-1]) { continue }
            if ($modSet -contains 'static') { continue }
            $mret = ($words -join ' ')
            if (-not $mret -or $mret.StartsWith('<')) { continue }
            $mret = ($mret -replace '\b([A-Z])\b', 'java.lang.Object') -replace '\$', '.'
            $mp = @()
            $praw2 = ($mm['params'] -replace '\b([A-Z])\b', 'java.lang.Object') -replace '\$', '.'
            if ($praw2.Trim()) {
                foreach ($p in (Split-TopCommas $praw2)) {
                    if ($p.Trim()) { $mp += $p.Trim() }
                }
            }
            $skey = $rnm + '/' + ((@($mp | ForEach-Object { Get-SimpleName $_ })) -join ',')
            if ($sigSeen.ContainsKey($skey)) { continue }
            $sigSeen[$skey] = $true
            $psig2 = Join-Params $mp
            if ($mret -eq 'void') {
                $lines.Add("${iind}public void $rnm($psig2) { }")
            } else {
                $lines.Add("${iind}public $mret $rnm($psig2) { return $(Default-Expr $mret); }")
            }
        }
    }
    if (-not $isIface -and -not $isEnum -and -not $hasNoArgCtor) {
        $cbody2 = '{ }'
        if ($parentNeedsSuper) { $cbody2 = '{ super(' + $parentSuperExpr + '); }' }
        $lines.Insert($headerIdx + 1, [string]::new(' ', $inner) + "public $simple() $cbody2")
    }
    if ($rest) { foreach ($m in [regex]::Matches($rest, 'opendoja\.compat\.[\w.]+')) { Queue-If-Compat $m.Value } }

    foreach ($kidName in ($node.kids.Keys | Sort-Object)) {
        $kidSlash = $slashBinary + '$' + $kidName
        Emit-Node $node.kids[$kidName] $kidSlash $kidName $true $inner $lines
    }
    $lines.Add("$ind}")
    $lines.Add("")
}

# ---------------- auto-expand queue ----------------
$needJavapBinary = New-Object System.Collections.ArrayList
$realIfaceCache = @{}

function Ensure-Kids([string]$t) {
    $best = ''
    foreach ($ek in $expandedTops.Keys) {
        if ($t.StartsWith($ek + '.') -and $ek.Length -gt $best.Length) { $best = $ek }
    }
    if (-not $best) { Write-Output ('[ek-nobest] ' + $t); return }
    $rem = $t.Substring($best.Length + 1)
    $rawSlash = (Unmap-Dotted $best) -replace '\.', '/'
    if ($tree.ContainsKey($rawSlash)) { $topSlash = $rawSlash } else { $topSlash = Map-Slash $rawSlash }
    if (-not $tree.ContainsKey($topSlash)) { Write-Output ('[ek-notree] ' + $t + ' -> ' + $topSlash); return }
    $node = $tree[$topSlash]
    $segs = $rem.Split('.')
    foreach ($seg in $segs) {
        if (-not $node.kids.ContainsKey($seg)) { $node.kids[$seg] = New-Node }
        $node = $node.kids[$seg]
    }
    $orig = (Unmap-Dotted $best) + '$' + ($segs -join '$')
    Write-Output ('[ek-ok] ' + $t + ' kid=' + ($segs -join '$'))
    if (-not $classHeaders.ContainsKey($orig)) {
        if (-not ($needJavapBinary -contains $orig)) { [void]$needJavapBinary.Add($orig) }
    }
}

function Get-RealIfaceMethods([string]$fqn) {
    if ($realIfaceCache.ContainsKey($fqn)) { return $realIfaceCache[$fqn] }
    $res = New-Object System.Collections.ArrayList
    $out = & 'D:\Android Studio\jbr\bin\javap.exe' -protected $fqn
    foreach ($l in $out) {
        $t = $l.Trim()
        if ($t -notmatch ';$') { continue }
        if ($t -notmatch '\(') { continue }
        [void]$res.Add($t)
    }
    $realIfaceCache[$fqn] = $res
    return $res
}

function Queue-AllThrows {
    foreach ($hv in @($throwsMap.Values) + @($throwsGlobal.Values)) {
        if (-not $hv) { continue }
        foreach ($tx in ($hv -split ',')) {
            $m = [regex]::Match($tx.Trim(), '[A-Za-z_][\w.$]*')
            if ($m.Success) {
                $v = $m.Value
                if ($v -like 'java.awt.*' -or $v -like 'javax.sound.*' -or $v -like 'javax.swing.*') { Queue-If-Compat (Map-Dotted $v) }
            }
        }
    }
}

$realParentInfo = @{}
function Get-ParentSuper([string]$fqn) {
    if ($realParentInfo.ContainsKey($fqn)) { return $realParentInfo[$fqn] }
    $info = @{ noArg = $true; expr = '' }
    $simple = ($fqn -split '\.')[-1]
    $out = & 'D:\Android Studio\jbr\bin\javap.exe' -protected $fqn
    $bestN = -1; $bestExpr = ''
    foreach ($l in $out) {
        $t = $l.Trim()
        if ($t -notmatch ';$') { continue }
        if ($t -notmatch '([\w.$]+)\(') { continue }
        $nm = (($Matches[1]) -split '\.')[-1]
        if ($nm -ne $simple) { continue }
        $pi = $t.IndexOf('('); $pe = $t.IndexOf(')')
        $praw = $t.Substring($pi + 1, $pe - $pi - 1)
        if (-not $praw.Trim()) { $info.noArg = $true; $info.expr = ''; $realParentInfo[$fqn] = $info; return $info }
        $exprs = @()
        foreach ($p in (Split-TopCommas $praw)) {
            $pt = $p.Trim()
            while ($pt -match '<') { $n2 = $pt; $pt = $pt -replace '<[^<>]*>', ''; if ($pt -eq $n2) { break } }
            $pt = ($pt -split '\s+')[-1]
            $exprs += (Default-Expr $pt)
        }
        if ($bestN -lt 0 -or $exprs.Count -lt $bestN) { $bestN = $exprs.Count; $bestExpr = ($exprs -join ', ') }
    }
    if ($bestN -gt 0) { $info.noArg = $false; $info.expr = $bestExpr }
    $realParentInfo[$fqn] = $info
    return $info
}

# fetch missing class headers for inventory tops (jdk-sigs-all only carries a subset)
$missingHdr = @()
foreach ($tk in @($tree.Keys)) {
    $dk = Unmap-Dotted (($tk -replace '/', '.'))
    if (-not $classHeaders.ContainsKey($dk)) { $missingHdr += $dk }
}
if ($missingHdr.Count -gt 0) {
    Write-Output ('[hdr] fetch missing ' + $missingHdr.Count)
    $hlines = & 'D:\Android Studio\jbr\bin\javap.exe' -protected @missingHdr
    $hdrFound = 0
    foreach ($hl in $hlines) {
        if ($hl -match '^\S.*\s\{$' -and $hl -match '(?:class|interface|enum)\s+([A-Za-z0-9_.$]+)') {
            $classHeaders[$Matches[1]] = $hl
            $hdrFound++
        }
    }
    Write-Output ('[hdr] headers stored ' + $hdrFound)
}
# seed classes visible only via wildcard imports in retained sources
$wildFiles = @()
$srcJava0 = Join-Path $root 'opendoja\src\main\java'
foreach ($sf in Get-ChildItem $srcJava0 -Recurse -Filter *.java) {
    $txt = [IO.File]::ReadAllText($sf.FullName)
    $pks = @()
    foreach ($im in [regex]::Matches($txt, 'import\s+(opendoja\.compat\.(?:awt|sound|swing)(?:\.\w+)*)\.\*\s*;')) {
        $pks += $im.Groups[1].Value
    }
    if ($pks.Count -gt 0) { $wildFiles += , @($txt, $pks) }
}
$candSet = @{}
foreach ($wf in $wildFiles) {
    foreach ($pkg in $wf[1]) {
        foreach ($m in [regex]::Matches($wf[0], '\b([A-Z]\w*)\b')) {
            $nmS = $m.Groups[1].Value
            $cf2 = Join-Path $srcJava0 (($pkg -replace '\.', '\') + '\' + $nmS + '.java')
            if (Test-Path $cf2) { continue }
            $candSet[($pkg + '.' + $nmS)] = $true
        }
    }
}
if ($candSet.Count -gt 0) {
    $cl = @($candSet.Keys | ForEach-Object { Unmap-Dotted $_ } | Sort-Object)
    $ch = @()
    $eapB = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    for ($ci = 0; $ci -lt $cl.Count; $ci += 300) {
        $chunk = $cl[$ci..[Math]::Min($ci + 299, $cl.Count - 1)]
        $ch += @(& 'D:\Android Studio\jbr\bin\javap.exe' -protected @chunk)
    }
    $ErrorActionPreference = $eapB
    $seenB = @{}
    foreach ($l in $ch) { if ($l -match '(?:class|interface|enum)\s+([A-Za-z0-9_.$]+)') { $seenB[$Matches[1]] = $true } }
    foreach ($b in $seenB.Keys) { Queue-If-Compat (Map-Dotted ($b -replace '\$', '.')) }
    Write-Output ('[seed] wildcards cand=' + $cl.Count + ' valid=' + $seenB.Count)
}
Write-Output '[4] queue built'; Queue-AllThrows; for ($round = 0; $round -lt 6; $round++) {
    $queueSnapshot = @{}
    while ($pending.Count -gt 0) {
        $t = $pending.Dequeue()
        if ($queueSnapshot.ContainsKey($t)) { continue }
        $queueSnapshot[$t] = $true
    }
    if ($queueSnapshot.Count -eq 0) { break }

    # determine which need new files
    $needJavap = @()
    foreach ($t in $queueSnapshot.Keys) {
        $top = Get-TopDotted $t
        if ($expandedTops.ContainsKey($top)) { continue }
        # nested under an existing top?
        $known = $false
        foreach ($ek in $expandedTops.Keys) {
            if ($t.StartsWith($ek + '.')) { $known = $true; break }
        }
        if ($known) { continue }
        $needJavap += (Unmap-Dotted $t)
    }
    if ($needJavap.Count -eq 0) { continue }
    Write-Output ('[j] javap ' + $needJavap.Count); $sigs = & 'D:\Android Studio\jbr\bin\javap.exe' -protected @needJavap
    $found = @{}
    $current = $null
    foreach ($line in $sigs) {
        if ($line -match '^\S.*\s\{$') {
            if ($line -match '(?:class|interface|enum)\s+([A-Za-z0-9_.$]+)') {
                $current = $Matches[1]
                $classHeaders[$current] = $line
                $found[$current] = $true
            }
            continue
        }
        if (-not $current) { continue }
        $t2 = $line.Trim()
        if ($t2 -notmatch ';$' -or $t2 -match '^\w*\s*static\s*\{') { continue }
        if ($t2 -notmatch '([\w.$]+)\(') { continue }
        $pi = $t2.IndexOf('('); $pe = $t2.IndexOf(')')
        $nm2 = (($Matches[1]) -split '\.')[-1]
        $throws = ''
        if ($t2 -match '\)\s*throws\s+([^;]+);') { $throws = $Matches[1] }
        $paramsRaw = $t2.Substring($pi + 1, $pe - $pi - 1)
        $simp = @()
        if ($paramsRaw.Trim()) {
            $tmp = $paramsRaw
            while ($tmp -match '<') { $n2 = $tmp; $tmp = $tmp -replace '<[^<>]*>', ''; if ($tmp -eq $n2) { break } }
            foreach ($p in ($tmp -split ',')) { $simp += Get-SimpleName $p }
        }
        $key = $nm2 + '/' + ($simp -join ',')
        $throwsMap[($current + '|' + $key)] = $throws
        if (-not $throwsGlobal.ContainsKey($key)) { $throwsGlobal[$key] = $throws
        Queue-AllThrows }
    }
    # register found types into tree as empty nodes
    foreach ($bk in $found.Keys) {
        $slash = $bk -replace '\.', '/'
        $slash = Map-Slash $slash
        $topSlash = $bk -replace '\.', '/'
        $i = $topSlash.IndexOf('$')
        if ($i -ge 0) { $topSlash = $topSlash.Substring(0, $i) }
        $topSlash = Map-Slash $topSlash
        $node = $tree
        $parts = $topSlash.Split('/')
        if (-not $tree.ContainsKey($topSlash)) {
            # package parts: opendoja/compat/... validate package path
            $tree[$topSlash] = New-Node
        }
        # nested placement
        $bkTop = $bk
        $bi = $bk.IndexOf('$')
        if ($bi -ge 0) {
            $chain = $bk.Substring($bi + 1).Split('$')
            $tn = $tree[$topSlash]
            foreach ($seg in $chain) {
                if (-not $tn.kids.ContainsKey($seg)) { $tn.kids[$seg] = New-Node }
                $tn = $tn.kids[$seg]
            }
        }
        $expandedTops[(Map-Dotted ($topSlash -replace '/', '.'))] = $true
        # queue header-referenced types
        $h = $classHeaders[$bk]
        if ($h -match '(class|interface|enum)\s+[\w.$]+(?<rest>.*)$') {
            foreach ($mm in [regex]::Matches($Matches['rest'], 'java\.awt\.[\w.$]+|javax\.sound\.[\w.$]+|javax\.swing\.[\w.$]+')) {
                Queue-If-Compat (Map-Dotted $mm.Value.Replace('$', '.'))
            }
        }
    }
    # unknown ones that javap failed: create as empty classes
    foreach ($u in $needJavap) {
        $mapped = Map-Dotted $u
        $top = Get-TopDotted $mapped
        if ($expandedTops.ContainsKey($top)) { continue }
        $known = $false
        foreach ($ek in $expandedTops.Keys) { if ($mapped.StartsWith($ek + '.')) { $known = $true; break } }
        if ($known) { continue }
        if ($u -match '^java\.awt\.' -or $u -match '^javax\.sound\.' -or $u -match '^javax\.swing\.') {
            $slash = Map-Slash ($u -replace '\.', '/')
            $tree[$slash] = New-Node
            $expandedTops[$top] = $true
            "WARN created empty for javap-miss: $u"
        }
    }
}

Write-Output '[3] rounds1 done'; # queue types referenced by inventory row descriptors first
foreach ($r in $shimRows) {
    $mi = $r.member.LastIndexOf(':')
    if ($mi -lt 0) { continue }
    $desc = $r.member.Substring($mi + 1)
    if ($desc.StartsWith('(')) {
        $md = Convert-MethodDesc $desc
        Queue-If-Compat $md.ret
        foreach ($pp in $md.params) { Queue-If-Compat $pp }
    } else {
        $cv = Convert-FieldDesc $desc
        if ($r.type -like '*RenderingHints*') { Write-Output ('[desc] ' + $desc + ' -> ' + $cv) }
        Queue-If-Compat $cv
    }
}
# header-referenced types of inventory tops
foreach ($k in @($tree.Keys)) {
    $dk = ($k -replace '/', '.')
    $sigK = $dk
    if ($classHeaders.ContainsKey($sigK)) {
        $h = $classHeaders[$sigK]
        if ($h -match '(class|interface|enum)\s+[\w.$]+(?<rest>.*)$') {
            foreach ($mm in [regex]::Matches($Matches['rest'], 'java\.awt\.[\w.$]+|javax\.sound\.[\w.$]+|javax\.swing\.[\w.$]+')) {
                Queue-If-Compat (Map-Dotted $mm.Value.Replace('$', '.'))
            }
        }
    }
}
Write-Output '[4] queue built'; Queue-AllThrows; for ($round = 0; $round -lt 6; $round++) {
    $queueSnapshot = @{}
    while ($pending.Count -gt 0) { $t = $pending.Dequeue(); $queueSnapshot[$t] = $true }
    if ($queueSnapshot.Count -eq 0) { break }
    $needJavap = @()
    foreach ($t in $queueSnapshot.Keys) {
        $top = Get-TopDotted $t
        if ($expandedTops.ContainsKey($top)) { Ensure-Kids $t; continue }
        $known = $false
        foreach ($ek in $expandedTops.Keys) { if ($t.StartsWith($ek + '.')) { $known = $true; break } }
        if ($known) { Ensure-Kids $t; continue }
        $needJavap += (Unmap-Dotted $t)
    }
    if ($needJavapBinary.Count -gt 0) {
        $needJavap += @($needJavapBinary)
        $needJavapBinary.Clear()
    }
    if ($needJavap.Count -eq 0) { continue }
    Write-Output ('[j] javap ' + $needJavap.Count); $sigs = & 'D:\Android Studio\jbr\bin\javap.exe' -protected @needJavap
    $found = @{}
    $current = $null
    foreach ($line in $sigs) {
        if ($line -match '^\S.*\s\{$') {
            if ($line -match '(?:class|interface|enum)\s+([A-Za-z0-9_.$]+)') {
                $current = $Matches[1]
                $classHeaders[$current] = $line
                $found[$current] = $true
            }
            continue
        }
        if (-not $current) { continue }
        $t2 = $line.Trim()
        if ($t2 -notmatch ';$' -or $t2 -match '^\w*\s*static\s*\{') { continue }
        if ($t2 -notmatch '([\w.$]+)\(') { continue }
        $pi = $t2.IndexOf('('); $pe = $t2.IndexOf(')')
        $nm2 = (($Matches[1]) -split '\.')[-1]
        $throws = ''
        if ($t2 -match '\)\s*throws\s+([^;]+);') { $throws = $Matches[1] }
        $paramsRaw = $t2.Substring($pi + 1, $pe - $pi - 1)
        $simp = @()
        if ($paramsRaw.Trim()) {
            $tmp = $paramsRaw
            while ($tmp -match '<') { $n2 = $tmp; $tmp = $tmp -replace '<[^<>]*>', ''; if ($tmp -eq $n2) { break } }
            foreach ($p in ($tmp -split ',')) { $simp += Get-SimpleName $p }
        }
        $key = $nm2 + '/' + ($simp -join ',')
        $throwsMap[($current + '|' + $key)] = $throws
        if (-not $throwsGlobal.ContainsKey($key)) { $throwsGlobal[$key] = $throws
        Queue-AllThrows }
    }
    foreach ($bk in $found.Keys) {
        $rawTop = ($bk -replace '\.', '/')
        $i = $rawTop.IndexOf('$')
        if ($i -ge 0) { $rawTop = $rawTop.Substring(0, $i) }
        if ($tree.ContainsKey($rawTop)) { $topSlash = $rawTop } else { $topSlash = Map-Slash $rawTop }
        if (-not $tree.ContainsKey($topSlash)) { $tree[$topSlash] = New-Node }
        $bi = $bk.IndexOf('$')
        if ($bi -ge 0) {
            $chain = $bk.Substring($bi + 1).Split('$')
            $tn = $tree[$topSlash]
            foreach ($seg in $chain) {
                if (-not $tn.kids.ContainsKey($seg)) { $tn.kids[$seg] = New-Node }
                $tn = $tn.kids[$seg]
            }
        }
        $expandedTops[(Map-Dotted ((Map-Slash $rawTop) -replace '/', '.'))] = $true
        $h = $classHeaders[$bk]
        if ($h -match '(class|interface|enum)\s+[\w.$]+(?<rest>.*)$') {
            foreach ($mm in [regex]::Matches($Matches['rest'], 'java\.awt\.[\w.$]+|javax\.sound\.[\w.$]+|javax\.swing\.[\w.$]+')) {
                Queue-If-Compat (Map-Dotted $mm.Value.Replace('$', '.'))
            }
        }
    }
    foreach ($u in $needJavap) {
        $mapped = Map-Dotted $u
        $top = Get-TopDotted $mapped
        if ($expandedTops.ContainsKey($top)) { continue }
        $known = $false
        foreach ($ek in $expandedTops.Keys) { if ($mapped.StartsWith($ek + '.')) { $known = $true; break } }
        if ($known) { continue }
        if ($u -match '^java\.awt\.' -or $u -match '^javax\.sound\.' -or $u -match '^javax\.swing\.') {
            $slash = Map-Slash ($u -replace '\.', '/')
            $tree[$slash] = New-Node
            $expandedTops[$top] = $true
            "WARN created empty for javap-miss: $u"
        }
    }
}

Write-Output '[5] rounds2 done';
# ---------------- constants: static final compile-time values (folded into bytecode) ----------------
function ConvertTo-JdkName([string]$key) {
    $k = $key -replace '/', '.'
    if ($k.StartsWith('opendoja.compat.awt.')) { return 'java.awt.' + $k.Substring('opendoja.compat.awt.'.Length) }
    if ($k.StartsWith('opendoja.compat.sound.')) { return 'javax.sound.' + $k.Substring('opendoja.compat.sound.'.Length) }
    if ($k.StartsWith('opendoja.compat.swing.')) { return 'javax.swing.' + $k.Substring('opendoja.compat.swing.'.Length) }
    return $k
}
$constArgs = New-Object System.Collections.ArrayList
$binToNode = @{}
function Collect-Consts($node, [string]$bin) {
    [void]$constArgs.Add($bin)
    $binToNode[$bin] = $node
    foreach ($kn in @($node.kids.Keys)) { Collect-Consts $node.kids[$kn] ($bin + '$' + $kn) }
}
foreach ($tk in @($tree.Keys)) { Collect-Consts $tree[$tk] (ConvertTo-JdkName $tk) }
Write-Output ('[7] const-javap ' + $constArgs.Count)
$cout = & 'D:\Android Studio\jbr\bin\javap.exe' -constants -protected @constArgs
$curNode = $null
$nC = 0
foreach ($l in $cout) {
    if ($l -match '^\S.*\s\{$') {
        $curNode = $null
        if ($l -match '(?:class|interface|enum)\s+([A-Za-z0-9_.$]+)') {
            $cb = $Matches[1]
            if ($binToNode.ContainsKey($cb)) { $curNode = $binToNode[$cb] }
        }
        continue
    }
    if (-not $curNode) { continue }
    $t2 = $l.Trim()
    if ($t2 -match '^(?:public|protected|private)\s+static\s+final\s+(?<ty>[\w.$]+(?:\[\])*)\s+(?<nm>\w+)\s*=\s*(?<val>[^;]+);$') {
        [void]$curNode.cfields.Add($Matches['ty'] + '|' + $Matches['nm'] + '|' + $Matches['val'].Trim())
        $nC++
    }
}
Write-Output ('[7] constants parsed: ' + $nC) # ---------------- write all files ----------------
Write-Output ('[6] write start, tops=' + $tree.Count); foreach ($top in ($tree.Keys | Sort-Object)) {
    Emit-File $top
}
"generated files: " + $written.Count
"top-level types: " + $tree.Count

# ---------------- restore hand-written shims (wiped with $outRoot) ----------------
$manualRoot = Join-Path $root 'compat-manual'
if (Test-Path $manualRoot) {
    $mfiles = @(Get-ChildItem $manualRoot -Recurse -Filter *.java)
    foreach ($mf in $mfiles) {
        $rel = $mf.FullName.Substring($manualRoot.Length + 1)
        $dst = Join-Path $srcJava $rel
        $ddir = Split-Path $dst -Parent
        if (-not (Test-Path $ddir)) { New-Item -ItemType Directory -Force -Path $ddir | Out-Null }
        Copy-Item $mf.FullName $dst -Force
    }
    Write-Output ('[m] manual shims restored: ' + $mfiles.Count)
}
