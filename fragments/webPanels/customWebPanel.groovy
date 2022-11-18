package path.to.file

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import org.apache.log4j.Level
import org.apache.log4j.Logger
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.onresolve.jira.groovy.user.FieldBehaviours
import groovy.transform.BaseScript
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import groovy.xml.MarkupBuilder
import com.atlassian.jira.issue.link.IssueLink;
import groovy.text.markup.MarkupTemplateEngine
import freemarker.template.*

def issue = context.issue as Issue

/* 
    #CSS styles taken from official Atlassian aui API
    
    https://aui.atlassian.com/aui/7.9/docs

    log your stuff
    def log = Logger.getLogger("com.onresolve.scriptrunner.runner.ScriptRunnerImpl")
    log.setLevel(Level.DEBUG)
*/

def issueManager = ComponentAccessor.getIssueManager()
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def projectManager = ComponentAccessor.getProjectManager()
def issueLinkManager = ComponentAccessor.getIssueLinkManager()
def links = issueLinkManager.getOutwardLinks(issue.id)
def leTable = new StringWriter()
def xml = new MarkupBuilder(leTable)
def customFieldA = customFieldManager.getCustomFieldObjectsByName('CustomField A')
def customFieldB = customFieldManager.getCustomFieldObjectsByName('CustomField B')
def customFieldC = customFieldManager.getCustomFieldObjectsByName('CustomField C')

if(!links){
    return null
}

       
def parentID = issue.getId()
def moreButton = "/jira/secure/CreateSubTaskIssue!default.jspa?parentIssueId=" + parentID.toString()


xml.table(class: "aui aui-table-sortable aui-navgroup"){
    
    tr(){
        //th( "Key")
        th("Summary")
        th("CustomField A")
        th("CustomField B")
        th("CustomField C")
        th('Status')
        th(){a(class: "issueaction-create-subtask aui-icon aui-icon-small aui-iconfont-add issueaction-aui-icon", href:moreButton)}
    }
   links.each 
    {
        issueLink ->

        if (issueLink.getIssueLinkType().subTaskLinkType) {
            def linkedIssue = issueLink.destinationObject
            def key = linkedIssue.getKey().toString()
            def subIssue = issueManager.getIssueByCurrentKey(linkedIssue.getKey())
            def valueCFA = subIssue.getCustomFieldValue(customFieldA)
            def valueCFB = subIssue.getCustomFieldValue(customFieldB)
            def valueCFC = subIssue.getCustomFieldValue(customFieldC)
            def summary = subIssue.getSummary()
            def status = subIssue.getStatus().getName()
            def tdClass = "aui-nav"
            tr{
                    td(class:"aui-table-column-issue-key") {
                        a (href:"https://yourJiraServer/jira/browse/$key",target:'_blank') {
                        mkp.yield summary
                        }
                    }
                    td(class:tdClass) {
                        p valueCFA
                    }
                    td(class:tdClass) {
                        p valueCFB
                    }
                    td(class:tdClass) {
                        p valueCFC
                    }
                    td(class:tdClass){
                        //default grey button or lozenge xD
                        def statusClass = 'jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-blue-gray jira-issue-status-lozenge-new aui-lozenge-subtle jira-issue-status-lozenge-max-width-short'
                        if (status.toLowerCase() == 'closed') {
                            //make status button green when closed
                            statusClass = 'jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-green jira-issue-status-lozenge-done aui-lozenge-subtle jira-issue-status-lozenge-max-width-short'
                        } 
                        span(class: statusClass, status) 
                    }
            }   

        }
        
    }
}


writer.write(leTable.toString())