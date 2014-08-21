@echo off
if "%1" == "" goto error
pushd %~dp0\bin
java molfile2wurcs.CUI_main %*
popd
goto end
:error
echo バッチファイルにmolfile、sdfile、もしくはこれらの入ったディレクトリをドラッグアンドドロップする。
:end
