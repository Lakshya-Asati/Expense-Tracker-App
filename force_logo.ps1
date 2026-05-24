Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Image]::FromFile("C:\Users\Lakshya\OneDrive\Desktop\LogoE.jpeg")
$img.Save("C:\Users\Lakshya\AndroidStudioProjects\ExpenseTracker\app\src\main\res\drawable\brand_logo_final.png", [System.Drawing.Imaging.ImageFormat]::Png)
$img.Dispose()