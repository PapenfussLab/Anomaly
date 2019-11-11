db.createUser(
  {
    user : "mongouser",
    pwd : "mongopass567",
    roles: [
		{ role: "dbOwner", db: "anomalydb" },
                { role: "readWrite", db: "anomalydb" }
           ]
  }
)
