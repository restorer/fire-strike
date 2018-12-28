var file = (function() {
	var active = false;
	var levelsList = [];

	function refreshLevelsList(prevSelected) {
		if (active) {
			return;
		}

		active = true;
		levelsList = [];

		if (!window.SERVER_URL) {
			lib.query('#levels-list').set('innerHTML', '<option value="">SERVER_URL not configured</option>');
			return;
		}

		if (!prevSelected) {
			prevSelected = lib.query('#levels-list').get('value');
		}

		lib.query('#levels-list')
			.set('disabled', true)
			.set('innerHTML', '<option value="">Loading ...</option>');

		lib.ajax({
			url: window.SERVER_URL,
			data: {
				mode: 'list'
			},
			callback: function(data) {
				active = false;

				if (data == null) {
					alert('Server error');
				} else if (data.error) {
					alert(data.error);
				} else {
					levelsList = data.data;

					if (levelsList.length) {
						var html = '';

						for (var i = 0; i < levelsList.length; i++) {
							html += '<option value="' + lib.htmlEscape(levelsList[i]) + '"';

							if (prevSelected == levelsList[i]) {
								html += ' selected="selected"';
							}

							html += '>' + lib.htmlEscape(levelsList[i]) + '</option>';
						}

						lib.query('#levels-list')
							.set('disabled', false)
							.set('innerHTML', html);
					} else {
						lib.query('#levels-list').set('innerHTML', '<option value="">Empty list</option>');
					}
				}
			}
		});
	}

	function loadLevel() {
		var name = lib.query('#levels-list').get('value');

		if (active || name == '' || name == null || !window.SERVER_URL) {
			return;
		}

		active = true;

		lib.ajax({
			url: SERVER_URL,
			data: {
				mode: 'load',
				name: name
			},
			callback: function(data) {
				active = false;

				if (data == null) {
					alert('Server error');
				} else if (data.error) {
					alert(data.error);
				} else {
					data = updateDataFormat(data.data);
					// paths.changeGraphicsSet(data.graphics);

					lib.query('#actions').set('value', data.actions);
					lib.query('#graphics-set').set('value', data.graphics);
					lib.query('#ensure').set('value', typeof data.ensureLevel === 'number' ? data.ensureLevel : 2);
					lib.query('#difficulty').set('value', data.difficultyLevel ? data.difficultyLevel : 1);

					var undoMap = map.getMapCopy();

					for (var i = 0; i < map.MAX_HEIGHT; i++) {
						for (var j = 0; j < map.MAX_WIDTH; j++) {
							cell.clear(map.m[i][j]);
						}
					}

					map.restoreFromBuffer(data.map, data.xpos, data.ypos, undoMap);
					map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);

					map.centerViewAt(data.xpos + Math.floor(data.map[0].length / 2), data.ypos + Math.floor(data.map.length / 2));
					file.hideSubDialog();

					lib.query('#save-level-name').set('value', lib.query('#levels-list').get('value'));
				}
			}
		});
	}

	function updateDataFormat(data) {
		// nothing to update for fire strike
		return data;
	}

	function saveLevel() {
		var name = String(lib.query('#save-level-name').get('value')).trim();
		var bounds = map.getBounds();

		if (active || name == '' || bounds == null || !window.SERVER_URL) {
			return;
		}

		active = true;

		var ensureLevel = parseInt(lib.query('#ensure').dom().value, 10);
		var difficultyLevel = parseFloat(lib.query('#difficulty').dom().value);

		lib.ajax({
			url: SERVER_URL,
			data: {
				mode: 'save',
				name: name,
				data: {
					format: 4,
					xpos: bounds.sx,
					ypos: bounds.sy,
					map: map.copyToBuffer(bounds.sx, bounds.sy, bounds.ex, bounds.ey),
					graphics: 'set-1', // paths.graphicsSet,
					ensureLevel: ensureLevel,
					difficultyLevel: difficultyLevel,
					actions: lib.query('#actions').get('value')
				}
			},
			callback: function(data) {
				active = false;

				if (data == null) {
					alert('Server error');
				} else if (data.error) {
					alert(data.error);
				} else {
					refreshLevelsList(name);
					file.hideSubDialog();
				}
			}
		});
	}

	return {
		init: function() {
			refreshLevelsList();

			lib.query('.b-refresh-list').on('click', function() { refreshLevelsList(); });
			lib.query('.b-load-level').on('click', loadLevel);
			lib.query('.b-save-level').on('click', saveLevel);

			lib.query('.b-clear-level').on('click', function() {
				var undoMap = map.getMapCopy();

				for (var i = 0; i < map.MAX_HEIGHT; i++) {
					for (var j = 0; j < map.MAX_WIDTH; j++) {
						cell.clear(map.m[i][j]);
					}
				}

				map.addToUndo(undoMap);
				map.tmpMap = null;

				map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);
				file.hideSubDialog();
			});

			lib.query('#levels-list').on('change', function() {
				lib.query('#save-level-name').set('value', this.value);
				loadLevel();
			});
		},

		hideSubDialog: function() {
			lib.query('.b-save-load').removeClass('active');
			lib.query('.b-save-load-sub').hide();
		}
	};
})();
