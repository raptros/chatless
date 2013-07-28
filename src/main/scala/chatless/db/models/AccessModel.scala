package chatless.db.models

import chatless.db.mfields.MField

trait AccessModel { self =>

  type MFld = MField[_, self.type]

  def fields:List[MFld]

  lazy val fieldMap:Map[String, MFld] = fields groupBy { _.name } mapValues { _.head }


}
