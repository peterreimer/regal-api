package authenticate;

public enum Role {
	ADMIN, /* Admin is allowed to trigger all API actions. */
	EDITOR, /*
					 * Editor is allowed to create and modify objects. Everything under
					 * /resource is visible for the editor
					 */
	READER, /*
					 * The reader visits the system from a certain IP The reader can read
					 * all data flagged with "public","restricted","remote" The reader is
					 * not able to perform modifying, creating, or deleting operations
					 */
	SUBSCRIBER, /*
							 * The subscriber behaves like the reader but can als access date
							 * flagged with "single"
							 */
	REMOTE, /*
					 * The remote user behaves like the reader
					 */
	GUEST /*
				 * The anonymous user can read everything flagged with "public".
				 */
}
