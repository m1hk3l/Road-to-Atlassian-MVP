/*
This script works from jira 8 and upwards
*/

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.issue.search.SearchQuery;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.AttachmentManager;
import org.apache.log4j.Logger
import org.apache.log4j.Level

def log = Logger.getLogger("com.onresolve.scriptrunner.runner.ScriptRunnerImpl")
log.setLevel(Level.DEBUG)


def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
def searchProvider = ComponentAccessor.getComponent(SearchProvider)
def issueManager = ComponentAccessor.getIssueManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

def jqlQuery = "project = YOUR_PROJECT_KEY and resolution is not empty and attachments is not EMPTY  and updated < -180d"

def query = jqlQueryParser.parseQuery(jqlQuery)
def luceneQuery = SearchQuery.create(query, user)
def results = searchProvider.search(luceneQuery, PagerFilter.getUnlimitedFilter()).getResults()*.document
.collect{ComponentAccessor.issueManager.getIssueObject(it.getField('issue_id').stringValue().toLong())}
results.each { 
    issue ->
       def attachments = ComponentAccessor.attachmentManager.getAttachments(issue)
       attachments.each {attachment ->
          if (attachment.getFilesize() > 10000000) {
              logger.warn(issue.getKey()+" "+attachment.getFilename()+":"+attachment.getFilesize().toString())
       ComponentAccessor.attachmentManager.deleteAttachment(attachment)
            }
      }
}
