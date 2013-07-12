# chatless

connectionless restful chat api

## paths

### user
1 through 3 - anywhere there is a uid. note that "/me" is shorthand for "/:<uid of logged in user>";
8 through 9 - only if :uid is that of logged in user

1. [/:uid]/ -> user handle
2. [/:uid]/nick
3.   [/:uid]/public -> boolean

    yes: other users can see my info etc, follows are autoapproved.

    no: only current followers can see anything below, all requests require approval

4. [/:uid]/info -> user info object
5. [/:uid]/following -> user list
6. [/:uid]/followers -> user list
7. [/:uid]/topics -> topic handle list (only ones the logged in user can see)
8. [/:uid]/blocked -> user list
9. [/:uid]/tags -> tags list

### topics
2 and above - only if topic is public or user is participating:

1. /topics/ -> the topics the user is participating in (see [/:uid]/topics)
2. /topics/:tid -> topic object {id:<tid>, op:<uid>, title:<string>}
3. /topics/:tid/title -> string (only if the logged in user is in the topic participants list)
4. /topics/:tid/public -> boolean
5. /topics/:tid/info -> object
6. /topics/:tid/tags -> tags list
7. /topics/:tid/participants -> user list

### messages
1. /topics/:id/messages -> messages queue
2. /topics/:id/messages/:id -> get a message object

### events
1. /events/ -> events queue (for currently logged-in user)
2. /events/:id/ -> get event at id

### requests
1. /requests/ -> request queue

### tags
1. /tag/:string -> topic list (any public topic with this tag

