from pathlib import Path
import re

root = Path('runtime')

# Add the game JAR as a dependency and remove release-signing requirements.
p = root / 'app/build.gradle'
s = p.read_text()
if "src/midlet/libs/game.jar" not in s:
    s = s.replace("dependencies {\n", "dependencies {\n    implementation files('src/midlet/libs/game.jar')\n", 1)
s = re.sub(r'\n\s*signingConfigs\s*\{.*?\n\s*\}\n\s*\nbuildTypes', '\n\n    buildTypes', s, count=1, flags=re.S)
s = re.sub(r'\n\s*signingConfig\s+signingConfigs\.release', '', s)
p.write_text(s)

# Standalone profile: original 240x320 canvas, portrait, fullscreen.
p = root / 'app/src/main/java/javax/microedition/shell/MicroLoader.java'
s = p.read_text()
old = '''\tpublic boolean init() {
\t\tFile config = new File(workDir + Config.MIDLET_CONFIGS_DIR + appDirName);
\t\tthis.params = ProfilesManager.loadConfig(config);
\t\tif (params == null) {
\t\t\treturn false;
\t\t}
'''
new = '''\tpublic boolean init() {
\t\tif (BuildConfig.FULL_EMULATOR) {
\t\t\tFile config = new File(workDir + Config.MIDLET_CONFIGS_DIR + appDirName);
\t\t\tthis.params = ProfilesManager.loadConfig(config);
\t\t\tif (params == null) return false;
\t\t} else {
\t\t\tthis.params = new ProfileModel(new File(workDir + Config.MIDLET_CONFIGS_DIR + appDirName));
\t\t\tthis.params.screenWidth = 240;
\t\t\tthis.params.screenHeight = 320;
\t\t\tthis.params.screenScaleType = 2;
\t\t\tthis.params.screenScaleRatio = 100;
\t\t\tthis.params.screenGravity = 2;
\t\t\tthis.params.orientation = 2;
\t\t\tthis.params.forceFullscreen = true;
\t\t\tthis.params.showKeyboard = false;
\t\t\tthis.params.touchInput = true;
\t\t}
'''
if old not in s:
    raise SystemExit('MicroLoader init block not found')
p.write_text(s.replace(old, new, 1))

# Add the custom white D-pad overlay above the game.
p = root / 'app/src/main/java/javax/microedition/shell/MicroActivity.java'
s = p.read_text()
marker = '\t\tsetContentView(view);\n\t\tsetSupportActionBar(binding.toolbar);'
repl = marker + '''
\t\tif (!BuildConfig.FULL_EMULATOR) {
\t\t\tandroid.view.ViewGroup root = (android.view.ViewGroup) binding.getRoot();
\t\t\tStandaloneTouchControls controls = new StandaloneTouchControls(this);
\t\t\troot.addView(controls, new android.view.ViewGroup.LayoutParams(-1, -1));
\t\t}'''
if marker not in s:
    raise SystemExit('MicroActivity setup not found')
p.write_text(s.replace(marker, repl, 1))

# Do not modify Canvas.java: the game remains a real portrait 240x320 image.
print('Standalone portrait fullscreen patch applied successfully.')
