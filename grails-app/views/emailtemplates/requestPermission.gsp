<%@ page contentType="text/html"%>

Hi ${curator.name.capitalize()},
<br/><br/>
User <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id) }">${user.name.capitalize()}</g:link> is requesting to be a ${cu?curator:contributor} for - , that you curate on <b>${domain}</b>.
<br/> 

Please <a href="${uri}" title="Confirmation code">click here</a> to confirm the ${cu?curatorship:contributorship}.<br/>
You may communicate directly with <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> at ${user.email}.
<br/><br/>
Thank you,<br/>
The Portal Team
