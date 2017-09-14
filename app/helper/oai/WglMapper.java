package helper.oai;

import models.DublinCoreData;
import models.Node;

public class WglMapper {

	OaiDcMapper dcMapper;

	public WglMapper(Node node) {
		dcMapper = new OaiDcMapper(node);
	}

	public DublinCoreData getData() {
		return dcMapper.getData();
	}

}
