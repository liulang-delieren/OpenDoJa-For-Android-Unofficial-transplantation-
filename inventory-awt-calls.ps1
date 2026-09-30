[Console]::OutputEncoding = [Text.Encoding]::UTF8
$b = Join-Path $PSScriptRoot 'opendoja\src\main\java'
$files = Get-ChildItem $b -Recurse -Filter *.java | Where-Object { $_.FullName -notmatch '\\probes\\|\\tools\\|\\launcher\\' }
$all = ($files | ForEach-Object { [IO.File]::ReadAllText($_.FullName) }) -join "`n"
$pats = @(
  'union\(', 'intersection\(', '\.intersects\(', '\.grow\(',
  'createConcatenated\(', '\.invert\(', 'inverseTransform\(', '\.getMatrix\(',
  'setToIdentity\(', 'getRotateInstance\(', 'getScaleInstance\(', 'getTranslateInstance\(', 'getShearInstance\(',
  '\.shear\(', '\.determinant\(', '\.concatenate\(', '\.preConcatenate\(', '\.rotate\(', '\.scale\(',
  'deriveFont\(', 'stringWidth\(', 'charWidth\(', 'getStringBounds\(',
  '\.getRGB\(', '\.setRGB\(', 'createGraphics\(', 'drawImage\(', 'getSubimage\(', '\.getRaster\(',
  'copyArea\(', 'drawOval\(', 'fillOval\(', 'drawRoundRect\(', 'fillRoundRect\(', 'drawArc\(', 'fillArc\(',
  'drawString\(', 'drawGlyphVector\(', '\.create\(\)', 'getFontMetrics\(', 'getClipBounds\(',
  'setClip\(', 'clipRect\(', '\bgetClip\(', 'setComposite\(', 'getComposite\(',
  'setTransform\(', 'getTransform\(', '\btransform\(', 'setRenderingHint\(', '\btranslate\(',
  'new AffineTransform', 'AffineTransform\.', 'AlphaComposite\.', 'Color\.', 'BufferedImage\(',
  '\.union\(', 'getLocation\(', 'setBounds\(', 'containsPoint|\.contains\('
)
foreach ($p in $pats) {
  $c = [regex]::Matches($all, $p).Count
  if ($c -gt 0) { '{0} = {1}' -f $p, $c }
}
Write-Output '=== AffineTransform construction sites ==='
foreach ($f in $files) {
  $c = [IO.File]::ReadAllText($f.FullName)
  foreach ($m in [regex]::Matches($c, '.{0,60}new AffineTransform.{0,60}')) {
    ($m.Value -replace '\s+', ' ') + '   << ' + $f.Name
  }
}
