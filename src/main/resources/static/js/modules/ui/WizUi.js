import {default as FilesInput} from "../utils/FilesInput.js?version=@version@";
import {default as SelectUser} from "../utils/SelectUser.js?version=@version@";
import {Step} from "../../prototypes/Step.js?version=@version@";
import {ExternalUserInfos} from "../../prototypes/ExternalUserInfos.js?version=@version@";

export class WizUi {

    constructor(workflowId, div, workflowName, csrf, maxSize) {
        this.workflowId = workflowId;
        this.signBookId = "";
        this.div = div;
        this.workflowName = workflowName;
        if(workflowName === "" || workflowName == null) {
            this.workflowName = "custom";
        }
        this.csrf = csrf;
        this.maxSize = maxSize;
        this.mode = "";
        this.input;
        this.fileInput;
        this.recipientCCSelect;
        this.form;
        this.close = false;
        this.end = false;
        this.start = false;
        this.modal = $('#' + this.div.attr('id').replace("Frame", "Modal"));
        $('#addNew').hide();
        this.initListeners();
    }

    initListeners() {
        this.modal.on('hidden.bs.modal', e => this.checkOnModalClose());
    }

    submitStartWorkflow() {
        let self = this;
        let form = $("#start-workflow-form");
        let title = $("#title-wiz");
        if(this.workflowName === "custom" && title.val() === "") {
            console.log("submit form custom");
            $(window).on('scroll', function(e) {
                window.scrollTo(0,0);
            });
            $("#title-wiz-submit").click();
        } else {
            console.log("send post " + form.attr("action"));
            $.post({
                url: form.attr("action"),
                data: form.serialize(),
                success: function () {
                    self.input.fileinput("upload");
                }
            });
        }
    }

    checkOnModalClose() {
        let workflowId = $("#wizWorkflowId").val();
        if(this.signBookId || workflowId) {
            let self = this;
            bootbox.confirm("Attention si vous fermez cette fenêtre, les modifications seront perdues", function(result) {
                if(result) {
                    if (workflowId != null) {
                        $.ajax({
                            method: "DELETE",
                            url: "/user/workflows/silent-delete/" + workflowId + "?" + self.csrf.parameterName + "=" + self.csrf.token,
                            cache: false
                        });
                    } else {
                        console.log(self.signBookId);
                        $.ajax({
                            method: "DELETE",
                            url: "/user/signbooks/silent-delete/" + self.signBookId + "?" + self.csrf.parameterName + "=" + self.csrf.token,
                            cache: false
                        });
                    }
                    self.modal.modal('hide');
                    self.modal.unbind();
                    self.div.html("");
                } else {
                    self.modal.modal('show');
                }
            });
        }
    }

    startByDocs() {
        console.info("Start wizard signbook");
        $.ajax({
            type: "GET",
            url: '/user/wizard/wiz-start-by-docs?workflowId=' + this.workflowId,
            dataType : 'html',
            cache: false,
            success : html => this.initWiz1(html)
        });
    }

    startByRecipients() {
        console.info("Start wizard workflow");
        this.mode = "-workflow";
        $.ajax({
            type: "GET",
            url: '/user/wizard/wiz-init-steps-workflow',
            dataType : 'html',
            cache: false,
            success : html => this.initWiz2(html)
        });
    }

    initWiz1(html) {
        let self = this;
        this.div.html(html);
        $("#wiz-start-button").on('click', e => this.submitStartWorkflow());
        this.input = $("#multipartFiles_" + this.workflowId);
        if(!this.workflowId) this.input = $("#multipartFiles_0");
        this.fileInput = new FilesInput(this.input, this.maxSize, this.csrf, this.workflowName, null, false);
        this.input.on("filebatchuploadcomplete", function(event, data, previewId, index, fileId) {
            self.gotoStep2(data.response);
        });
        let id = this.workflowId;
        if(id === "") {
            id = 0;
        }
        this.recipientCCSelect = new SelectUser("recipientsCCEmailsWiz" + id, null, null, this.csrf);
        this.form = this.div.find('form');
        this.form.on('submit', function(e){
            e.preventDefault();
            let url = self.form.attr('action') + "?" + self.csrf.parameterName + '=' + self .csrf.token;
            let formData = new FormData(this);
            $.ajax( {
                url: url,
                method: 'POST',
                type: "POST",
                data: formData,
                processData: false,
                contentType: false,
                success: result => self.gotoStep2(result),
                error: function(request, status, error) {
                    alert(request.responseText);
                }
            } );
        });

    }

    gotoStep2(e) {
        console.log("goto step 2");
        let comment = $("#commentWiz");
        let title = $("#title-wiz");
        let id = this.workflowId;
        if(id === "") {
            id = 0;
        }
        let recipientsCCEmailsWiz=[];
        $('select[name="recipientsCCEmailsWiz' + id + '"] option:selected').each(function() {
            recipientsCCEmailsWiz.push($(this).val());
        });
        let forceAllSign = $('input[name="forceAllSign2"]').is(":checked");
        this.div.html("");
        this.signBookId = e;
        $.ajax({
            type: "GET",
            url: '/user/wizard/wiz-init-steps?workflowId=' + id + "&recipientsCCEmailsWiz=" + recipientsCCEmailsWiz + "&forceAllSign=" + forceAllSign + "&comment=" + encodeURIComponent(comment.val()) + "&title=" + title.val(),
            dataType : 'html',
            cache: false,
            success : html => this.initWiz2(html)
        });
    }

    initWiz2(html) {
        let self = this;
        this.div.html(html);
        if($("#recipientsEmailsWiz").length) {
            new SelectUser("recipientsEmailsWiz", null, null, this.csrf);
        }
        $('[id^="recipientEmailsWizSelect_"]').each(function (){
            new SelectUser($(this).attr('id'), null, null, self.csrf);
        });
        if($("#targetEmailsSelect").length) {
            new SelectUser("targetEmailsSelect", null, null, this.csrf);
        }
        if($("#recipientsCCEmailsWiz").length) {
            this.recipientCCSelect = new SelectUser("recipientsCCEmailsWiz", null, null, this.csrf);
        }
        $("#end").on('click', function (){
            self.end = true;
        });
        $("#endStart").on('click', function (){
            self.end = true;
            self.start = true;
        });
        $("#exitWiz").on('click', e => this.exit());
        $("#saveWorkflow").on('click', e => this.saveWorkflow(e));
        $("#wiz-step-form").on('submit', e => this.gotoAddStep(e));
    }

    gotoAddStep(e) {
        e.preventDefault();
        let csrf = this.csrf;
        let step = new Step();
        step.workflowId = $('#wizWorkflowId').val();
        step.recipientsEmails = $('#recipientsEmailsWiz').find(`[data-es-check-cert='true']`).prevObject[0].slim.getSelected();
        step.allSignToComplete = $('#allSignToCompleteWiz').is(':checked');
        step.changeable = $('#changeableWiz').is(':checked');
        step.autoSign = $('#autoSign').is(':checked');
        let userSignFirst = $('#userSignFirstWiz').is(':checked');
        step.signType = $('#signTypeWiz').val();
        $("div[id^='externalUserInfos_']").each(function() {
            let externalUserInfos = new ExternalUserInfos();
            externalUserInfos.email = $(this).find("#emails").val();
            externalUserInfos.name = $(this).find("#names").val();
            externalUserInfos.firstname = $(this).find("#firstnames").val();
            externalUserInfos.phone = $(this).find("#phones").val();
            externalUserInfos.forcesms = $(this).find("#forcesmses").val();
            step.externalUsersInfos.push(externalUserInfos);
        });
        let signBookId = this.signBookId;
        console.log(signBookId);
        let self = this;
        $.ajax({
            url: "/user/wizard/wiz-add-step"+ this.mode +"?end=" + self.end + "&userSignFirst=" + userSignFirst + "&start=" + self.start + "&close=" + self.close + "&" + csrf.parameterName + "=" + csrf.token,
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify(step),
            success: html => this.initWiz2(html),
            error: function(data){
                console.error(data.responseJSON.message);
                bootbox.alert("Une erreur s'est produite. Merci de vérifier votre saisie", function (){ });
            }
        });
    }

    saveWorkflow(e) {
        let saveWorkflowForm = $("#saveWorkflowForm");
        if (saveWorkflowForm.find('.required').filter(function(){ return this.value === '' }).length > 0) {
            $("#saveWorkflowSubmit").click();
            return false;
        }

        let csrf = this.csrf;
        let name = $("#workflowName").val();
        let elementId = $("#elementId");
        let self = this;
        $.ajax({
            url: "/user/wizard/wiz-save"+ this.mode +"/" + elementId.val() + "?name=" + name + "&viewers=" + self.recipientCCSelect.slimSelect.getSelected() + "&" + csrf.parameterName + "=" + csrf.token,
            type: 'POST',
            success: html => this.initWiz2(html)
        });
    }

    exit() {
        let csrf = this.csrf;
        if(this.signBookId !== ""){
            $.ajax({
                url: "/user/wizard/wizend?name=" + name + "&close=" + $('#close').val() + "&" + csrf.parameterName + "=" + csrf.token,
                type: 'POST',
                success: html => this.initWiz2(html)
            });
        } else {
            $.ajax({
                url: "/user/wizard/wiz-save-workflow/" + $('#wizWorkflowId').val(),
                type: 'GET',
                success: html => this.initWiz2(html)
            });
        }

    }

}