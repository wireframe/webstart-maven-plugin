<SCRIPT LANGUAGE="JavaScript"> 
var javawsInstalled = 0;  
var javaws142Installed=0;
var javaws150Installed=0;
var javaws160Installed = 0;
isIE = "false"; 
if (navigator.mimeTypes && navigator.mimeTypes.length) { 
   supportsJnlp = navigator.mimeTypes['application/x-java-jnlp-file']; 
   if (supportsJnlp || navigator.userAgent.indexOf("Gecko") != -1) { 
      javawsInstalled = 1; 
      javaws142Installed=1;
      javaws150Installed=1;
      javaws160Installed = 1; 
  } 
} else { 
   isIE = "true"; 
} 
</SCRIPT> 

<SCRIPT LANGUAGE="VBScript">
on error resume next
If isIE = "true" Then
  If Not(IsObject(CreateObject("JavaWebStart.isInstalled"))) Then
     javawsInstalled = 0
  Else
     javawsInstalled = 1
  End If
  If Not(IsObject(CreateObject("JavaWebStart.isInstalled.1.4.2.0"))) Then
     javaws142Installed = 0
  Else
     javaws142Installed = 1
  End If 
  If Not(IsObject(CreateObject("JavaWebStart.isInstalled.1.5.0.0"))) Then
     javaws150Installed = 0
  Else
     javaws150Installed = 1
  End If  
  If Not(IsObject(CreateObject("JavaWebStart.isInstalled.1.6.0.0"))) Then
     javaws160Installed = 0
  Else
     javaws160Installed = 1
  End If  
End If
</SCRIPT>
