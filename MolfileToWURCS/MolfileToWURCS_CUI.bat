@echo off
if "%1" == "" goto error
pushd %~dp0\bin
java molfile2wurcs.CUI_main %*
popd
goto end
:error
echo �o�b�`�t�@�C����molfile�Asdfile�A�������͂����̓������f�B���N�g�����h���b�O�A���h�h���b�v����B
:end
