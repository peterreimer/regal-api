scm_info(){
    git_info=`git branch 2>/dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/\1/'`
	if [ "${git_info}" ]; then
		if [ "$(git status -s)" ]; then
			git_color='\033[1;31m'
		else
			git_color='\033[1;36m'
		fi
		echo -e "${git_color}git:${git_info}"
	fi
}

PS1='\n\[\033[1;32m\][\w] $(scm_info)\[\033[0m\]\n\$ '
