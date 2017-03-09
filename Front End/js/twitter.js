var searchForm = $('#search-form'),
    apiUrl = 'http://dijkstra.cs.ttu.ee:8080/twitter-api/',
    searchResults = $('#search-results'),
    historyResults = $('#history'),
    addLocationBtn = $('#add-location'),
    favouriteFormInfo = $('#favourite-form-info'),
    favouriteForm = $('#favourite-form'),
    favouriteResults = $('#favourite-results');

/* Working with search */
// Fetch tweets when search form is submitted
searchForm.submit(function(e) {
    // Prevent the default submit action
    e.preventDefault();
    // Log the input to console (For debugging)
    console.log(searchForm.serialize());
    // Perform search & populate results
    performSearch(searchForm.serialize());
});

var performSearch = function (searchParams) {
    // Clear all previous results
    searchResults.empty();

    $.ajax({
        type: 'GET',
        url: apiUrl + 'search',
        data: searchParams, // serializes the form's elements.
        success: function(data) {
            // Log the result to console (For debugging)
            console.log(data);
            // Generate and append the results HTML
            searchResults.append('<hr>' + getTweetsHtml(data.tweets));
            var query = data.query;
            if (!query['is_favourite']) {
                // Add button to add favourite result as favourite
                searchResults.append('<button class="add-favourite button-success pure-button" onclick="addNewFavourite(event)">Save as favourite</button>');

                // Hide query data in a hidden div
                searchResults.append('<div class="query-data hidden">' + JSON.stringify(query) + '</div>');
            }
        },
        error: function(error) {
            // Log the error to console (For debugging)
            console.log(error);
            // Populate failure response
            searchResults.html('<div class="no-result"> Failed to load results!</div>');
        }
    });
};

/* Working with History */

// Fetch history when history tab is clicked.
$('#history-tab').click(function (e) {
    e.preventDefault();
    historyResults.empty();
    $.ajax({
        type: 'GET',
        url: apiUrl + 'history',
        success: function (data) {
            console.log(data);
            $.each(data, function(index, history) {
                var tweets = [],
                    div = document.createElement('div'); // New div for each history item

                div.className = 'history-item';

                var html = '<span class="pull-left">Search location: ' + history.query.location + '</span>' +
                        '<span class="pull-right clearfix">Keyword: ' + history.query.keyword + '</span>';

                html += getTweetsHtml(history.results);

                // If it is not the first result, add a <hr>
                if (index !== 0)  html += '<hr>';

                // Append the generated
                $(div).html(html);

                // Add results in reverse order
                historyResults.prepend(div);
            });
        },
        error: function (error) {
            console.log(error);
            fadeInHtml(historyResults, '<div class="no-result"> Failed to load history!</div>');
        }
     });
});

/* Working with Favourites */

// Load favourites on switching tab
$('#favourites-tab').click(function (e) {
    e.preventDefault();
    fetchFavourites();
});

var fetchFavourites = function () {
    // Clear the current results
    favouriteResults.empty();
    $.ajax({
        type: 'GET',
        url: apiUrl + 'favourites',
        success: function (data) {
            console.log(data);
            $.each(data, function(location, value) {
                var div = document.createElement('div'); // New div for each favourite item

                div.className = 'favourite-item';

                var html = location +
                '<a class="delete-favourite pull-right clearfix" data-location="' + location + '" onclick="deleteFavourite(event)"> \
                    <button class="pure-button button-error"> \
                        <i class="fa fa-trash-o"></i><span> Delete </span> \
                    </button> \
                </a>' +
                '<a class="search-favourite pull-right clearfix" onclick="searchWithFavourite(event)"> \
                    <button class="pure-button button-success"> \
                        <i class="fa fa-search"></i><span> Search </span> \
                    </button> \
                </a>';

                // Hidden div to store the query parameters
                value.location = location;
                html += '<div class="favourite-value">' + JSON.stringify(value) + '</div>';
                $(div).html(html);

                favouriteResults.append(div);
            });
        },
        error: function (error) {
            console.log(error);
            fadeInHtml(favouriteResults, '<div class="no-result"> Failed to load favourites!</div>');
        }
     });
};

var deleteFavourite = function (e) {
    e.preventDefault();
    var parentAnchor = $(e.target).closest('.delete-favourite'),
        parentDiv = parentAnchor.closest('.favourite-item'),
        location = parentAnchor.attr('data-location');

    console.log(parentAnchor);
    $.ajax({
        type: 'POST',
        url: apiUrl + 'favourites/delete/' + location,
        success: function(data) {
            // Log the result to console (For debugging)
            console.log(data);
            // Remove the div from results
            parentDiv.remove();

            $.notify('Deleted successfully', {position: 'bottom center', className: 'success'});
        },
        error: function(error) {
            // Log the error to console (For debugging)
            console.log(error);

            // Global notification
            $.notify('Failed to delete favourite!', {position: 'bottom center'});

            var response = JSON.parse(error.responseText);
            if (response.error) {
                // Global notification
                $.notify(response.error, {position: 'bottom center'});
            }
            // Refresh the list of favourites
            fetchFavourites();
        }
    });
};

var searchWithFavourite = function (e) {
    e.preventDefault();

    // Rest the search form
    searchForm.trigger('reset');

    // Get form values
    var parentAnchor = $(e.target).closest('.search-favourite'),
        hiddenValues = JSON.parse(parentAnchor.next('.favourite-value').text()),
        newValues = {
            'location': hiddenValues.location,
            'query': hiddenValues.query_keyword,
            'count': hiddenValues.query_count
        };

    // Fill with new values
    $.each(newValues, function(key, value) {
        searchForm.find('[name="' + key + '"]').val(value);
    });

    // Switch to search tab
    $('#search-tab').parent('li').click();

    // Submit the form if it's valid
    if (searchForm[0].checkValidity()) {
        searchForm.submit();
    } else {
        notifyError(searchForm.find('[name="query"]'), 'Please fill this.');
    }
};

addLocationBtn.click(function (e) {
    notifyInfo(addLocationBtn, 'Fetching location...');
    // fadeInHtml(favouriteFormInfo, 'Fetching location...');
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(showFavouriteForm, showError);
    } else {
        fadeInHtml(favouriteFormInfo, 'Geolocation is not supported by this browser.');
    }
});

favouriteForm.submit(function (e) {
    e.preventDefault();
    console.log(favouriteForm.serializeArray());
    $.ajax({
        type: 'POST',
        url: apiUrl + 'favourites',
        data: favouriteForm.serializeArray(),
        success: function(data) {
            // Log the result to console (For debugging)
            console.log(data);
            $.notify('Saved successfully', {position: 'bottom center', className: 'success'});
            // Hide the form
            hideFavouriteForm();
            // Refresh the list of favourites
            fetchFavourites();
        },
        error: function(error) {
            // Log the error to console (For debugging)
            console.log(error);
            // Populate failure response
            fadeInHtml(favouriteFormInfo, 'Failed to save as favourite!');

            var response = JSON.parse(error.responseText);
            if (response.error) {
                notifyError(favouriteFormInfo, response.error, 'top center');
            }
            // Refresh the list of favourites
            fetchFavourites();
        }
    });
});

var showFavouriteForm = function(position) {
    // Clear any information
    clearNotifications();
    favouriteFormInfo.empty();
    // Hide the button
    addLocationBtn.hide().fadeOut();
    // Set the latitude & the longitude of the form
    favouriteForm.find('input[name="latitude"]').val(position.coords.latitude);
    favouriteForm.find('input[name="longitude"]').val(position.coords.longitude);
    // Display the form
    favouriteForm.show().fadeIn();
};

var showError = function (error) {
    console.log(error);
    switch(error.code) {
        case error.PERMISSION_DENIED:
            notifyError(addLocationBtn, 'User denied the request for Geolocation.');
            break;
        case error.POSITION_UNAVAILABLE:
            notifyError(addLocationBtn, 'Location information is unavailable.');
            break;
        case error.TIMEOUT:
            notifyError(addLocationBtn, 'The request to get user location timed out.');
            break;
        case error.UNKNOWN_ERROR:
            notifyError(addLocationBtn, 'An unknown error occurred.');
            break;
    }
};

var hideFavouriteForm = function() {
    // Hide the form
    favouriteForm.hide().fadeOut();
    // Reset it's values
    favouriteForm.trigger('reset');
    // Show the `Add my location` button
    addLocationBtn.show().fadeIn();
    favouriteFormInfo.empty();
};

var addNewFavourite = function(e) {
    e.preventDefault();

    // Rest the search form
    searchForm.trigger('reset');

    // Get form values
    var parentAnchor = $(e.target).closest('.add-favourite'),
        queryData = JSON.parse(parentAnchor.next('.query-data').text());

    // Overriding these because API requires those values
    queryData.query_keyword = queryData.keyword;
    queryData.query_count = queryData.count;

    $.ajax({
        type: 'POST',
        url: apiUrl + 'favourites',
        data: $.param(queryData),
        success: function(data) {
            // Log the result to console (For debugging)
            console.log(data);
            // Hide the button
            parentAnchor.hide();
            $.notify('Saved successfully', {position: 'bottom center', className: 'success'});
        },
        error: function(error) {
            // Log the error to console (For debugging)
            console.log(error);
            var response = JSON.parse(error.responseText);
            if (response.error) {
                notifyError(favouriteFormInfo, response.error, 'top center');
            }
        }
    });
};


/* Common methods */
var getTweetsHtml = function (tweets) {
    var html = '';
    $.each(tweets, function(index, tweet) {
        // Loop over all the tweets in this result and create a div for them
        html += '<div class="tweet">' + tweet.text + '<br><span class="pull-right">- @' +
            tweet.user + ' at ' + tweet.timestamp + '</span></div>';
    });

    return html;
};

var notifyError = function (element, error, position) {
    if (!position) {
        position = 'bottom center';
    }
    element.notify(error, {elementPosition: position});
};

var notifyInfo = function (element, info) {
    element.notify(info, {elementPosition: 'bottom center', className: 'info'});
};

var clearNotifications = function() {
    $('.notifyjs-wrapper').empty();
};

var fadeInHtml = function (element, html) {
    element.html(html).fadeIn(2);
};

