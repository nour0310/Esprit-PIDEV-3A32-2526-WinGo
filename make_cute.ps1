$path = 'c:\Users\noura\Desktop\PI\ProjetJava\src\main\resources\Event.fxml'
$content = Get-Content $path -Raw
$content = $content -replace 'rgba\(0,0,0,0\.22\)', 'rgba(255,240,245,0.7)'
$content = $content -replace 'rgba\(0,0,0,0\.28\)', 'rgba(255,182,193,0.4)'
$content = $content -replace 'rgba\(0,0,0,0\.32\)', 'rgba(255,228,225,0.6)'
$content = $content -replace 'rgba\(0,0,0,0\.30\)', 'rgba(255,228,225,0.6)'
$content = $content -replace 'rgba\(0,0,0,0\.4\)', 'rgba(255,192,203,0.6)'
$content = $content -replace 'rgba\(0,0,0,0\.5\)', 'rgba(255,255,255,0.8)'
$content = $content -replace 'rgba\(255,255,255,0\.92\)', '#ff69b4'
$content = $content -replace 'rgba\(255,255,255,0\.80\)', '#ff1493'
$content = $content -replace 'rgba\(255,255,255,0\.85\)', '#ff1493'
$content = $content -replace 'rgba\(255,255,255,0\.78\)', '#ff1493'
$content = $content -replace 'rgba\(255,255,255,0\.62\)', '#ffb6c1'
$content = $content -replace 'rgba\(255,255,255,0\.70\)', '#ffb6c1'
$content = $content -replace 'rgba\(255,255,255,0\.7\)', '#ffb6c1'

# Text and border tweaks
$content = $content -replace '-fx-text-fill: white;', '-fx-text-fill: #ff1493;'
$content = $content -replace '-fx-border-color: rgba\(255,255,255,0\.16\);', '-fx-border-color: rgba(255,105,180,0.40);'
$content = $content -replace '-fx-border-color: rgba\(255,255,255,0\.14\);', '-fx-border-color: rgba(255,105,180,0.40);'
$content = $content -replace '-fx-border-color: rgba\(255,255,255,0\.22\);', '-fx-border-color: rgba(255,105,180,0.40);'
$content = $content -replace '-fx-border-color: rgba\(255,255,255,0\.18\);', '-fx-border-color: rgba(255,105,180,0.40);'
$content = $content -replace '-fx-border-color: rgba\(255,255,255,0\.3\);', '-fx-border-color: rgba(255,105,180,0.40);'
$content = $content -replace '-fx-border-color: rgba\(255,255,255,0\.2\);', '-fx-border-color: rgba(255,105,180,0.40);'

# Special accents
$content = $content -replace 'rgba\(255,189,0,0\.28\)', '#ffb6c1'
$content = $content -replace 'rgba\(255,189,0,0\.18\)', '#ffe4e1'
$content = $content -replace 'rgba\(255,189,0,0\.35\)', '#ff69b4'
$content = $content -replace '#FFBD00', '#ff1493'
$content = $content -replace '#c49a6c', '#ffb6c1'

# Buttons solid colors
$content = $content -replace '#28a745', '#ff99cc'
$content = $content -replace '#ffc107', '#ffb6c1'
$content = $content -replace '#dc3545', '#ff66b2'
$content = $content -replace '#3498db', '#ff66b2'

# Background image
$content = $content -replace 'assets/amaan.jpg', 'assets/bg_wingo.png'

# Text content replacements / emojis
$content = $content.Replace('🏠', '🎀')
$content = $content.Replace('👤', '💖')
$content = $content.Replace('🔔', '✨')
$content = $content.Replace('⚙️', '🌸')
$content = $content.Replace('🔍', '🔮')
$content = $content.Replace('📅', '🌷')
$content = $content.Replace('👥', '👯')
$content = $content.Replace('📋', '📝')
$content = $content.Replace('🔐', '🔑')
$content = $content.Replace('🎫', '🎟️')
$content = $content.Replace('❤️', '💕')
$content = $content.Replace('🔥', '🌟')

$content | Set-Content -Encoding UTF8 $path
