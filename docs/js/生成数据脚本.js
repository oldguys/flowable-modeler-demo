var size = 20;
var keys = ["test-1104-1501","test-1104-1549"];

var result = {};

var itemList = []
result["itemList"] = itemList;

for (var i = 0; i < keys.length; i++) {
    var menuCode = keys[i];
    var item = {};
    item["key"] = menuCode;
    item["data"] = {};

    var sequence = [];
    item["sequence"] = sequence;
    for (var j = 0; j < size; j++) {
        sequence[j] = j + ""
    }

    itemList[i] = item;
}


var resultStr = JSON.stringify(result)

console.log(resultStr)