![appointmentSettingsForm](images/b04-appointmentSettingsForm.png)

![lowprivZonderAdminrechten](images/b04-lowprivZonderAdminrechten.png)

![b04-voor-runtime-insufficient-privileges](images/b04-voor-runtime-insufficient-privileges.png)

![b04-voor-controller-authorized-check](images/b04-voor-controller-authorized-check.png)
Ik heb de volgende command in powershell uitgevoerd voor deze uitkomst:
PS C:\Users\nickg\school\Lu2.4\LU2SecurityPoC> Get-ChildItem $controllerPath -Filter \*.java |

> > ForEach-Object {
> > $authorizedCount = (Select-String -Path $_.FullName -Pattern "@Authorized" -ErrorAction SilentlyContinue).Count
>>     [PSCustomObject]@{
>>         Controller = $_.Name
>>         AuthorizedAnnotations = $authorizedCount
>>     }
>> } | Format-Table -AutoSize
![b04-voor-context-isauthenticated](images/b04-voor-context-isauthenticated.png)
Ik heb de volgende command in powershell uitgevoerd voor deze uitkomst: 
PS C:\Users\nickg\school\Lu2.4\LU2SecurityPoC> Select-String -Path "$controllerPath\*.java" -Pattern "Context.isAuthenticated|@Authorized|RequestMapping" |
> > ForEach-Object {
> > [PSCustomObject]@{
> > File = Split-Path $_.Path -Leaf
> > Line = $_.LineNumber
> > Code = $\_.Line.Trim()
> > }
> > } | Format-Table -AutoSize -Wrap

![b04-na-unit-test-build-succes](images/b04-na-unit-test-build-succes.png)

![b04-na-controller-authorized-check](images/b04-na-controller-authorized-check.png)
