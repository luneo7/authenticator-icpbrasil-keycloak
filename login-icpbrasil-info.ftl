<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
    ${msg("loginTitle",(realm.displayName!''))}
    <#elseif section = "header">
    ${msg("loginTitleHtml",(realm.displayNameHtml!''))?no_esc}
    <#elseif section = "form">

    <form id="kc-icpbrasil-login-info" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
        <div class="${properties.kcFormGroupClass!}">

            <div class="${properties.kcLabelWrapperClass!}">
                <label for="certificate_subjectDN" class="${properties.kcLabelClass!}">Certificado Digital: </label>
            </div>
            <#if subjectDN??>
                <div class="${properties.kcLabelWrapperClass!}">
                    <label id="certificate_subjectDN" class="${properties.kcLabelClass!}">${(subjectDN!"")}</label>
                </div>
            <#else>
                <div class="${properties.kcLabelWrapperClass!}">
                    <label id="certificate_subjectDN" class="${properties.kcLabelClass!}">[Nenhum Certificado]</label>
                </div>
            </#if>
        </div>

        <div class="${properties.kcFormGroupClass!}">

            <#if isUserEnabled>
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="username" class="${properties.kcLabelClass!}">Você será autenticado como:</label>
                </div>
                <div class="${properties.kcLabelWrapperClass!}">
                    <label id="username" class="${properties.kcLabelClass!}">${(username!'')}</label>
                </div>
            </#if>

        </div>

        <div class="${properties.kcFormGroupClass!}">
            <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                <div class="${properties.kcFormOptionsWrapperClass!}">
                </div>
            </div>

            <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                <div class="${properties.kcFormButtonsWrapperClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="Continuar"/>
                    <#if isUserEnabled>
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}" name="cancel" id="kc-cancel" type="submit" value="Ignorar"/>
                    </#if>
                </div>
            </div>
            <span id="counter">O formulário será enviado em -- segundos</span>
        </div>
    </form>
    <script>

        var n = 10;
        function autoSubmitCountdown(){
            var c=n;
            setInterval(function(){
                if(c>=0){
                    document.getElementById("counter").textContent = "O formulário será enviado em " + c + " segundos";
                }
                if(c==0){
                    document.forms[0].submit();
                }
                c--;
            },1000);
        }

        // Start
        autoSubmitCountdown();

    </script>
    </#if>

</@layout.registrationLayout>
