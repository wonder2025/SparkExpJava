$( document ).ready(function() {
var myChart = echarts.init(document.getElementById('main'));
myChart.showLoading();
// $.getJSON("npmdepgraph.min10.json", function (json) {
// $.getJSON("callScoreJson.json", function (json) {
    $.getJSON("componentSet1223.json.json", function (json) {
    myChart.hideLoading();
    myChart.setOption(option = {
        title: {
            text: '用户连通图'
        },
        animation:false,
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series : [
            {
                type: 'graph',
                layout: 'none',
                // progressiveThreshold: 700,
                data: json.nodes.map(function (node) {
                    // alert(node.id)
                    return {
                        x: node.x,
                        y: node.y,
                        id: node.id,
                        name: node.label,
                        symbolSize: node.size,
                        itemStyle: {
                            normal: {
                                color: node.color
                            }
                        }
                    };
                }),
                edges: json.edges.map(function (edge) {
                    return {
                        source: edge.sourceID,
                        target: edge.targetID
                    };
                }),
                label: {
                    emphasis: {
                        position: 'right',
                        show: true
                    }
                },
                roam: true,
                focusNodeAdjacency: true,
                lineStyle: {
                    normal: {
                        width: 0.5,
                        curveness: 0.3,
                        opacity: 0.7
                    }
                }
            }
        ]
    }, true);
});
});
